import { createI18n } from 'vue-i18n'
import fr from './fr.json'
import en from './en.json'

const messages = {
  fr,
  en,
}

export type MessageLanguages = keyof typeof messages
export type MessageSchema = (typeof messages)['fr']

// Get locale from localStorage or default to French
const getLocale = (): MessageLanguages => {
  const stored = localStorage.getItem('locale')
  if (stored && Object.keys(messages).includes(stored)) {
    return stored as MessageLanguages
  }
  return 'fr'
}

export const i18n = createI18n({
  locale: getLocale(),
  fallbackLocale: 'fr',
  messages,
  legacy: false,
  globalInjection: true,
  missingWarn: false,
  fallbackWarn: false,
})

export const setLocale = (locale: MessageLanguages) => {
  i18n.global.locale.value = locale
  localStorage.setItem('locale', locale)
  document.documentElement.lang = locale
}

export const getAvailableLocales = () => Object.keys(messages) as MessageLanguages[]

export default i18n
