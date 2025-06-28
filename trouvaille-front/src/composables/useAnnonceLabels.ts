import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

/**
 * Composable for centralized annonce label mapping
 * Provides consistent translations for types, natures, statuses, etc.
 */
export function useAnnonceLabels() {
  const { t } = useI18n()

  /**
   * Type labels mapping
   */
  const typeLabels = computed(() => ({
    vente: t('annonce.types.vente'),
    location: t('annonce.types.location'),
  }))

  /**
   * Nature labels mapping
   */
  const natureLabels = computed(() => ({
    offre: t('annonce.natures.offre'),
    demande: t('annonce.natures.demande'),
  }))

  /**
   * Status labels mapping
   */
  const statusLabels = computed(() => ({
    active: t('annonce.status.active'),
    suspendue: t('annonce.status.suspendue'),
    vendue: t('annonce.status.vendue'),
    expiree: t('annonce.status.expiree'),
  }))

  /**
   * Period labels mapping for rentals
   */
  const periodLabels = computed(() => ({
    jour: t('annonce.periode.jour'),
    semaine: t('annonce.periode.semaine'),
    mois: t('annonce.periode.mois'),
  }))

  /**
   * Sort option labels mapping
   */
  const sortLabels = computed(() => ({
    date_creation_desc: t('annonce.search.sort.date_creation_desc'),
    date_creation_asc: t('annonce.search.sort.date_creation_asc'),
    prix_asc: t('annonce.search.sort.prix_asc'),
    prix_desc: t('annonce.search.sort.prix_desc'),
    titre_asc: t('annonce.search.sort.titre_asc'),
    titre_desc: t('annonce.search.sort.titre_desc'),
    distance_asc: t('annonce.search.sort.distance_asc'),
  }))

  /**
   * Get type label by key
   */
  const getTypeLabel = (type: string): string => {
    return typeLabels.value[type as keyof typeof typeLabels.value] || type
  }

  /**
   * Get nature label by key
   */
  const getNatureLabel = (nature: string): string => {
    return natureLabels.value[nature as keyof typeof natureLabels.value] || nature
  }

  /**
   * Get status label by key
   */
  const getStatusLabel = (status: string): string => {
    return statusLabels.value[status as keyof typeof statusLabels.value] || status
  }

  /**
   * Get period label by key
   */
  const getPeriodLabel = (period: string): string => {
    return periodLabels.value[period as keyof typeof periodLabels.value] || period
  }

  /**
   * Get sort option label by key
   */
  const getSortLabel = (sortKey: string): string => {
    return sortLabels.value[sortKey as keyof typeof sortLabels.value] || sortKey
  }

  /**
   * Get all type options for select components
   */
  const typeOptions = computed(() => [
    { value: '', label: t('common.actions.clear') },
    { value: 'vente', label: typeLabels.value.vente },
    { value: 'location', label: typeLabels.value.location },
  ])

  /**
   * Get all nature options for select components
   */
  const natureOptions = computed(() => [
    { value: '', label: t('common.actions.clear') },
    { value: 'offre', label: natureLabels.value.offre },
    { value: 'demande', label: natureLabels.value.demande },
  ])

  /**
   * Get all status options for select components
   */
  const statusOptions = computed(() => [
    { value: '', label: t('common.actions.clear') },
    { value: 'active', label: statusLabels.value.active },
    { value: 'suspendue', label: statusLabels.value.suspendue },
    { value: 'vendue', label: statusLabels.value.vendue },
  ])

  /**
   * Get all period options for select components
   */
  const periodOptions = computed(() => [
    { value: 'jour', label: periodLabels.value.jour },
    { value: 'semaine', label: periodLabels.value.semaine },
    { value: 'mois', label: periodLabels.value.mois },
  ])

  /**
   * Get all sort options for select components
   */
  const sortOptions = computed(() => [
    { value: 'date_creation_desc', label: sortLabels.value.date_creation_desc },
    { value: 'date_creation_asc', label: sortLabels.value.date_creation_asc },
    { value: 'prix_asc', label: sortLabels.value.prix_asc },
    { value: 'prix_desc', label: sortLabels.value.prix_desc },
    { value: 'titre_asc', label: sortLabels.value.titre_asc },
    { value: 'titre_desc', label: sortLabels.value.titre_desc },
    { value: 'distance_asc', label: sortLabels.value.distance_asc },
  ])

  return {
    // Direct label mappings
    typeLabels,
    natureLabels,
    statusLabels,
    periodLabels,
    sortLabels,

    // Label getters
    getTypeLabel,
    getNatureLabel,
    getStatusLabel,
    getPeriodLabel,
    getSortLabel,

    // Options for select components
    typeOptions,
    natureOptions,
    statusOptions,
    periodOptions,
    sortOptions,
  }
}

export default useAnnonceLabels