# PhotosApi

All URIs are relative to _http://localhost_

| Method                              | HTTP request                                 | Description                                            |
| ----------------------------------- | -------------------------------------------- | ------------------------------------------------------ |
| [**createPhoto**](#createphoto)     | **POST** /api/v1/annonces/photos             | Ajouter une photo                                      |
| [**deletePhoto**](#deletephoto)     | **DELETE** /api/v1/annonces/photos/{photoId} | Supprimer une photo                                    |
| [**getPhotoFull**](#getphotofull)   | **GET** /api/v1/photos/{photoId}/full        | Récupérer le contenu d\&#39;une photo en taille réelle |
| [**getPhotoThumb**](#getphotothumb) | **GET** /api/v1/photos/{photoId}/thumb       | Récupérer le contenu d\&#39;une photo en miniature     |

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

# **getPhotoFull**

> File getPhotoFull()

### Example

```typescript
import { PhotosApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new PhotosApi(configuration)

let photoId: string // (default to undefined)

const { status, data } = await apiInstance.getPhotoFull(photoId)
```

### Parameters

| Name        | Type         | Description | Notes                 |
| ----------- | ------------ | ----------- | --------------------- |
| **photoId** | [**string**] |             | defaults to undefined |

### Return type

**File**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: image/jpeg

### HTTP response details

| Status code | Description                          | Response headers |
| ----------- | ------------------------------------ | ---------------- |
| **200**     | Contenu de la photo en taille réelle | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getPhotoThumb**

> File getPhotoThumb()

### Example

```typescript
import { PhotosApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new PhotosApi(configuration)

let photoId: string // (default to undefined)

const { status, data } = await apiInstance.getPhotoThumb(photoId)
```

### Parameters

| Name        | Type         | Description | Notes                 |
| ----------- | ------------ | ----------- | --------------------- |
| **photoId** | [**string**] |             | defaults to undefined |

### Return type

**File**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: image/jpeg

### HTTP response details

| Status code | Description                      | Response headers |
| ----------- | -------------------------------- | ---------------- |
| **200**     | Contenu de la photo en miniature | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)
