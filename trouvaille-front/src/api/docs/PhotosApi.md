# PhotosApi

All URIs are relative to _http://localhost_

| Method                          | HTTP request                                      | Description                           |
| ------------------------------- | ------------------------------------------------- | ------------------------------------- |
| [**createPhoto**](#createphoto) | **POST** /api/v1/annonces/photos                  | Ajouter une photo                     |
| [**deletePhoto**](#deletephoto) | **DELETE** /api/v1/annonces/photos/{photoId}      | Supprimer une photo                   |
| [**getPhoto**](#getphoto)       | **GET** /api/v1/photos/{photoId}/{width}/{height} | Récupérer le contenu d\&#39;une photo |

# **createPhoto**

> Photo createPhoto(body)

### Example

```typescript
import { PhotosApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new PhotosApi(configuration)

let body: File //

const { status, data } = await apiInstance.createPhoto(body)
```

### Parameters

| Name     | Type     | Description | Notes |
| -------- | -------- | ----------- | ----- |
| **body** | **File** |             |       |

### Return type

**Photo**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/octet-stream
- **Accept**: application/json

### HTTP response details

| Status code | Description     | Response headers |
| ----------- | --------------- | ---------------- |
| **200**     | Photos ajoutées | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deletePhoto**

> deletePhoto()

### Example

```typescript
import { PhotosApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new PhotosApi(configuration)

let photoId: string // (default to undefined)

const { status, data } = await apiInstance.deletePhoto(photoId)
```

### Parameters

| Name        | Type         | Description | Notes                 |
| ----------- | ------------ | ----------- | --------------------- |
| **photoId** | [**string**] |             | defaults to undefined |

### Return type

void (empty response body)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: Not defined

### HTTP response details

| Status code | Description     | Response headers |
| ----------- | --------------- | ---------------- |
| **204**     | Photo supprimée | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getPhoto**

> File getPhoto()

### Example

```typescript
import { PhotosApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new PhotosApi(configuration)

let photoId: string // (default to undefined)
let width: number // (default to undefined)
let height: number // (default to undefined)
let accept: string // (optional) (default to undefined)

const { status, data } = await apiInstance.getPhoto(photoId, width, height, accept)
```

### Parameters

| Name        | Type         | Description | Notes                            |
| ----------- | ------------ | ----------- | -------------------------------- |
| **photoId** | [**string**] |             | defaults to undefined            |
| **width**   | [**number**] |             | defaults to undefined            |
| **height**  | [**number**] |             | defaults to undefined            |
| **accept**  | [**string**] |             | (optional) defaults to undefined |

### Return type

**File**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: image/\*

### HTTP response details

| Status code | Description         | Response headers |
| ----------- | ------------------- | ---------------- |
| **200**     | Contenu de la photo | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)
