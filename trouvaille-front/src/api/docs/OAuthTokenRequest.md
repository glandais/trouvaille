# OAuthTokenRequest

## Properties

| Name             | Type       | Description                    | Notes                  |
| ---------------- | ---------- | ------------------------------ | ---------------------- |
| **code**         | **string** | Code d\&#39;autorisation OAuth | [default to undefined] |
| **state**        | **string** | Paramètre state OAuth          | [default to undefined] |
| **redirect_uri** | **string** | URI de redirection             | [default to undefined] |

## Example

```typescript
import { OAuthTokenRequest } from '@trouvaille/api'

const instance: OAuthTokenRequest = {
  code,
  state,
  redirect_uri,
}
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
