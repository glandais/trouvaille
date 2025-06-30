# Annonce

## Properties

| Name                  | Type                                      | Description | Notes                             |
| --------------------- | ----------------------------------------- | ----------- | --------------------------------- |
| **type**              | [**AnnonceType**](AnnonceType.md)         |             | [default to undefined]            |
| **nature**            | [**AnnonceNature**](AnnonceNature.md)     |             | [default to undefined]            |
| **titre**             | **string**                                |             | [default to undefined]            |
| **description**       | **string**                                |             | [default to undefined]            |
| **prix**              | **number**                                |             | [default to undefined]            |
| **periode_location**  | [**PeriodeLocation**](PeriodeLocation.md) |             | [optional] [default to undefined] |
| **photos**            | **Array&lt;string&gt;**                   |             | [optional] [default to undefined] |
| **coordinates**       | [**Coordinates**](Coordinates.md)         |             | [default to undefined]            |
| **ville**             | **string**                                |             | [default to undefined]            |
| **id**                | **string**                                |             | [default to undefined]            |
| **statut**            | [**AnnonceStatut**](AnnonceStatut.md)     |             | [default to undefined]            |
| **utilisateur**       | [**Utilisateur**](Utilisateur.md)         |             | [default to undefined]            |
| **date_creation**     | **string**                                |             | [default to undefined]            |
| **date_modification** | **string**                                |             | [optional] [default to undefined] |

## Example

```typescript
import { Annonce } from '@trouvaille/api'

const instance: Annonce = {
  type,
  nature,
  titre,
  description,
  prix,
  periode_location,
  photos,
  coordinates,
  ville,
  id,
  statut,
  utilisateur,
  date_creation,
  date_modification,
}
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
