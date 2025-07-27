# TagsApi

All URIs are relative to _http://localhost_

| Method                                    | HTTP request         | Description                      |
| ----------------------------------------- | -------------------- | -------------------------------- |
| [**getAvailableTags**](#getavailabletags) | **GET** /api/v1/tags | Lister tous les tags disponibles |

# **getAvailableTags**

> Array<Tag> getAvailableTags()

### Example

```typescript
import { TagsApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new TagsApi(configuration)

const { status, data } = await apiInstance.getAvailableTags()
```

### Parameters

This endpoint does not have any parameters.

### Return type

**Array<Tag>**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details

| Status code | Description    | Response headers |
| ----------- | -------------- | ---------------- |
| **200**     | Liste des tags | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)
