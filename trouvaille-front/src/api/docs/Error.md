# ModelError

## Properties

| Name        | Type                                                       | Description | Notes                             |
| ----------- | ---------------------------------------------------------- | ----------- | --------------------------------- |
| **code**    | **string**                                                 |             | [default to undefined]            |
| **message** | **string**                                                 |             | [default to undefined]            |
| **details** | [**Array&lt;ErrorDetailsInner&gt;**](ErrorDetailsInner.md) |             | [optional] [default to undefined] |

## Example

```typescript
import { ModelError } from '@trouvaille/api'

const instance: ModelError = {
  code,
  message,
  details,
}
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
