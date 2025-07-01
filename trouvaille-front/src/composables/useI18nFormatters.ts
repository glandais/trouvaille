import { useI18n } from 'vue-i18n'

/**
 * Composable for internationalized formatting of dates, distances, and prices
 * Provides consistent, locale-aware formatting across the application
 */
export function useI18nFormatters() {
  const { t, locale } = useI18n()

  /**
   * Format relative dates with proper pluralization
   */
  const formatRelativeDate = (date: Date | string): string => {
    const targetDate = typeof date === 'string' ? new Date(date) : date
    const now = new Date()
    const diffInMs = now.getTime() - targetDate.getTime()
    const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24))

    if (diffInDays === 0) {
      return t('dates.today')
    } else if (diffInDays === 1) {
      return t('dates.yesterday')
    } else if (diffInDays < 7) {
      return t('dates.days_ago', { count: diffInDays })
    } else if (diffInDays < 30) {
      const weeks = Math.floor(diffInDays / 7)
      return t('dates.weeks_ago', { count: weeks })
    } else if (diffInDays < 365) {
      const months = Math.floor(diffInDays / 30)
      return t('dates.months_ago', { count: months })
    } else {
      const years = Math.floor(diffInDays / 365)
      return t('dates.years_ago', { count: years })
    }
  }

  /**
   * Format absolute dates with locale
   */
  const formatAbsoluteDate = (date: Date | string): string => {
    const targetDate = typeof date === 'string' ? new Date(date) : date
    return targetDate.toLocaleDateString(locale.value === 'fr' ? 'fr-FR' : 'en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  }

  /**
   * Format date with time
   */
  const formatDateTime = (date: Date | string): string => {
    const targetDate = typeof date === 'string' ? new Date(date) : date
    const dateStr = formatAbsoluteDate(targetDate)
    const timeStr = targetDate.toLocaleTimeString(locale.value === 'fr' ? 'fr-FR' : 'en-US', {
      hour: '2-digit',
      minute: '2-digit',
    })
    return `${dateStr} ${t('dates.at')} ${timeStr}`
  }

  /**
   * Smart date formatting - relative for recent dates, absolute for older ones
   */
  const formatSmartDate = (date: Date | string): string => {
    const targetDate = typeof date === 'string' ? new Date(date) : date
    const now = new Date()
    const diffInDays = Math.floor((now.getTime() - targetDate.getTime()) / (1000 * 60 * 60 * 24))

    // Use relative format for dates within the last 7 days
    if (diffInDays <= 7) {
      return formatRelativeDate(targetDate)
    }

    // Use absolute format for older dates
    return formatAbsoluteDate(targetDate)
  }

  /**
   * Format distance with proper units and locale
   */
  const formatDistance = (distanceInKm: number | undefined): string => {
    if (distanceInKm === undefined || distanceInKm === null) {
      return ''
    }

    // Round to 1 decimal place for distances < 10km, otherwise to nearest km
    const rounded =
      distanceInKm < 10 ? Math.round(distanceInKm * 10) / 10 : Math.round(distanceInKm)

    const formattedNumber = rounded.toLocaleString(locale.value === 'fr' ? 'fr-FR' : 'en-US', {
      minimumFractionDigits: rounded < 10 && rounded % 1 !== 0 ? 1 : 0,
      maximumFractionDigits: 1,
    })

    return `${formattedNumber} ${t('location.distance.km')}`
  }

  /**
   * Format price with proper currency and locale
   */
  const formatPrice = (
    price: number | undefined,
    periode?: string | null,
    showCurrency = true,
  ): string => {
    if (price === undefined || price === null) {
      return t('annonce.card.no_price')
    }

    // Format the number with locale-specific formatting
    const formattedNumber = price.toLocaleString(locale.value === 'fr' ? 'fr-FR' : 'en-US', {
      minimumFractionDigits: price % 1 === 0 ? 0 : 2,
      maximumFractionDigits: 2,
    })

    let result = showCurrency ? `${formattedNumber} €` : formattedNumber

    // Add period suffix for rentals
    if (periode && ['jour', 'semaine', 'mois'].includes(periode)) {
      result += ` ${t(`annonce.periode.${periode}`)}`
    }

    return result
  }

  /**
   * Format price range for filters
   */
  const formatPriceRange = (min?: number, max?: number): string => {
    if (!min && !max) return ''
    if (min && !max) return `> ${formatPrice(min, null, true)}`
    if (!min && max) return `< ${formatPrice(max, null, true)}`
    return `${formatPrice(min, null, true)} - ${formatPrice(max, null, true)}`
  }

  /**
   * Get localized type label
   */
  const getTypeLabel = (type: string): string => {
    return t(`annonce.types.${type}`, type)
  }

  /**
   * Get localized nature label
   */
  const getNatureLabel = (nature: string): string => {
    return t(`annonce.natures.${nature}`, nature)
  }

  /**
   * Get localized status label
   */
  const getStatusLabel = (status: string): string => {
    return t(`annonce.status.${status}`, status)
  }

  /**
   * Format file size for photo uploads
   */
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B'

    const k = 1024
    const sizes = locale.value === 'fr' ? ['B', 'KB', 'MB', 'GB'] : ['B', 'KB', 'MB', 'GB']

    const i = Math.floor(Math.log(bytes) / Math.log(k))
    const size = (bytes / Math.pow(k, i)).toFixed(1)

    return `${size} ${sizes[i]}`
  }

  /**
   * Pluralize with count (for manual pluralization when vue-i18n isn't enough)
   */
  const pluralize = (count: number, singular: string, plural?: string): string => {
    if (count <= 1) return singular
    return plural || `${singular}s`
  }

  return {
    // Date formatting
    formatRelativeDate,
    formatAbsoluteDate,
    formatDateTime,
    formatSmartDate,

    // Distance and location
    formatDistance,

    // Price formatting
    formatPrice,
    formatPriceRange,

    // Label mapping
    getTypeLabel,
    getNatureLabel,
    getStatusLabel,

    // Utility
    formatFileSize,
    pluralize,
  }
}

export default useI18nFormatters
