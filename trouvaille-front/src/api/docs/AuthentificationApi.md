# AuthentificationApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**exchangeOAuthToken**](#exchangeoauthtoken) | **POST** /api/v1/auth/token | Échanger le code OAuth pour un token|

# **exchangeOAuthToken**
> OAuthTokenResponse exchangeOAuthToken(oAuthTokenRequest)


### Example

```typescript
import {
    AuthentificationApi,
    Configuration,
    OAuthTokenRequest
} from '@trouvaille/api';

const configuration = new Configuration();
const apiInstance = new AuthentificationApi(configuration);

let oAuthTokenRequest: OAuthTokenRequest; //

const { status, data } = await apiInstance.exchangeOAuthToken(
    oAuthTokenRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **oAuthTokenRequest** | **OAuthTokenRequest**|  | |


### Return type

**OAuthTokenResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Token échangé avec succès |  -  |
|**400** | Requête invalide |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

