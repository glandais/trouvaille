# AnnonceBase

## Properties

| Name                 | Type                                      | Description | Notes                             |
| -------------------- | ----------------------------------------- | ----------- | --------------------------------- |
| **type**             | [**AnnonceType**](AnnonceType.md)         |             | [default to undefined]            |
| **nature**           | [**AnnonceNature**](AnnonceNature.md)     |             | [default to undefined]            |
| **titre**            | **string**                                |             | [default to undefined]            |
| **description**      | **string**                                |             | [default to undefined]            |
| **prix**             | **number**                                |             | [default to undefined]            |
| **periode_location** | [**PeriodeLocation**](PeriodeLocation.md) |             | [optional] [default to undefined] |
| **photos**           | [**Array&lt;Photo&gt;**](Photo.md)        |             | [default to undefined]            |
| **coordinates**      | [**Coordinates**](Coordinates.md)         |             | [default to undefined]            |
| **ville**            | **string**                                |             | [default to undefined]            |

## Example

```typescript
import { AnnonceBase } from '@trouvaille/api'

const instance: AnnonceBase = {
  type,
  nature,
  titre,
  description,
  prix,
  periode_location,
  photos,
  coordinates,
  ville,
}
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
