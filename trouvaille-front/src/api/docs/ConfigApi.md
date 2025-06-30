# ConfigApi

All URIs are relative to _http://localhost_

| Method                      | HTTP request           | Description                |
| --------------------------- | ---------------------- | -------------------------- |
| [**getConfig**](#getconfig) | **GET** /api/v1/config | Récupérer la configuration |

# **getConfig**

> FrontConfiguration getConfig()

### Example

```typescript
import { ConfigApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new ConfigApi(configuration)

const { status, data } = await apiInstance.getConfig()
```

### Parameters

This endpoint does not have any parameters.

### Return type

**FrontConfiguration**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details

| Status code | Description               | Response headers |
| ----------- | ------------------------- | ---------------- |
| **200**     | Détails de l\&#39;annonce | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)
