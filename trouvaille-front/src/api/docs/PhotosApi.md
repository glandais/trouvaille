# PhotosApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**createPhoto**](#createphoto) | **POST** /api/v1/annonces/photos | Ajouter une photo|
|[**deletePhoto**](#deletephoto) | **DELETE** /api/v1/annonces/photos/{photoId} | Supprimer une photo|

# **createPhoto**
> string createPhoto(body)


### Example

```typescript
import {
    PhotosApi,
    Configuration
} from '@trouvaille/api';

const configuration = new Configuration();
const apiInstance = new PhotosApi(configuration);

let body: File; //

const { status, data } = await apiInstance.createPhoto(
    body
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **File**|  | |


### Return type

**string**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: application/octet-stream
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Photos ajoutées |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deletePhoto**
> deletePhoto()


### Example

```typescript
import {
    PhotosApi,
    Configuration
} from '@trouvaille/api';

const configuration = new Configuration();
const apiInstance = new PhotosApi(configuration);

let photoId: string; // (default to undefined)

const { status, data } = await apiInstance.deletePhoto(
    photoId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **photoId** | [**string**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | Photo supprimée |  -  |
|**404** | Photo non trouvée |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

