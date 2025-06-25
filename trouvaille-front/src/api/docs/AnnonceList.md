# AnnonceList


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | [**AnnonceType**](AnnonceType.md) |  | [default to undefined]
**nature** | [**AnnonceNature**](AnnonceNature.md) |  | [default to undefined]
**titre** | **string** |  | [default to undefined]
**description** | **string** |  | [default to undefined]
**prix** | **number** |  | [optional] [default to undefined]
**periode_location** | [**PeriodeLocation**](PeriodeLocation.md) |  | [optional] [default to undefined]
**coordinates** | [**Coordinates**](Coordinates.md) |  | [default to undefined]
**distance** | **number** | Distance en kilomètres (présent uniquement si tri par distance) | [optional] [default to undefined]

## Example

```typescript
import { AnnonceList } from '@trouvaille/api';

const instance: AnnonceList = {
    type,
    nature,
    titre,
    description,
    prix,
    periode_location,
    coordinates,
    distance,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
