# AnnonceSearch

## Properties

| Name             | Type                                                    | Description                                         | Notes                             |
| ---------------- | ------------------------------------------------------- | --------------------------------------------------- | --------------------------------- |
| **type**         | [**AnnonceType**](AnnonceType.md)                       |                                                     | [optional] [default to undefined] |
| **statut**       | [**AnnonceStatut**](AnnonceStatut.md)                   |                                                     | [optional] [default to undefined] |
| **nature**       | [**AnnonceNature**](AnnonceNature.md)                   |                                                     | [optional] [default to undefined] |
| **page**         | **number**                                              | Numéro de page                                      | [optional] [default to 1]         |
| **limit**        | **number**                                              | Nombre d\&#39;éléments par page                     | [optional] [default to 20]        |
| **search**       | **string**                                              | Recherche textuelle dans titre et description       | [optional] [default to undefined] |
| **user_id**      | **string**                                              | Recherche par utilisateur                           | [optional] [default to undefined] |
| **prix_min**     | **number**                                              | Prix minimum                                        | [optional] [default to undefined] |
| **prix_max**     | **number**                                              | Prix maximum                                        | [optional] [default to undefined] |
| **latitude**     | **number**                                              | Latitude pour le tri par distance                   | [optional] [default to undefined] |
| **longitude**    | **number**                                              | Longitude pour le tri par distance                  | [optional] [default to undefined] |
| **distance_max** | **number**                                              | Distance maximale en kilomètres (nécessite lat/lon) | [optional] [default to undefined] |
| **sort_by**      | [**AnnonceSearchSortBy**](AnnonceSearchSortBy.md)       |                                                     | [optional] [default to undefined] |
| **sort_order**   | [**AnnonceSearchSortOrder**](AnnonceSearchSortOrder.md) |                                                     | [optional] [default to undefined] |

## Example

```typescript
import { AnnonceSearch } from '@trouvaille/api'

const instance: AnnonceSearch = {
  type,
  statut,
  nature,
  page,
  limit,
  search,
  user_id,
  prix_min,
  prix_max,
  latitude,
  longitude,
  distance_max,
  sort_by,
  sort_order,
}
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
