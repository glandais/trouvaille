import { 
  AnnonceList as BaseAnnonceList, 
  Annonce as BaseAnnonce,
  Pagination as BasePagination,
  Utilisateur as BaseUtilisateur,
  AnnonceStatut 
} from '../api'

// Extended interfaces to handle missing properties and naming inconsistencies
export interface AnnonceList extends BaseAnnonceList {
  photos?: string[]
  statut?: AnnonceStatut
  date_creation?: string
  date_modification?: string
  utilisateur?: Utilisateur
}

export interface Annonce extends BaseAnnonce {
  photos?: string[]
  date_modification?: string
}

export interface Pagination extends BasePagination {
  page_courante: number
  total_pages: number
  total_elements: number
  elements_par_page: number
}

export interface Utilisateur extends BaseUtilisateur {
  pseudo?: string
}

// Re-export common types
export * from '../api'