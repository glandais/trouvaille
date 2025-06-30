# AnnoncesApi

All URIs are relative to _http://localhost_

| Method                              | HTTP request                           | Description                         |
| ----------------------------------- | -------------------------------------- | ----------------------------------- |
| [**countAnnonces**](#countannonces) | **POST** /api/v1/annonces/search/count | Récupérer le nombre d\&#39;annonces |
| [**createAnnonce**](#createannonce) | **POST** /api/v1/annonces              | Créer une nouvelle annonce          |
| [**deleteAnnonce**](#deleteannonce) | **DELETE** /api/v1/annonces/{id}       | Supprimer une annonce               |
| [**getAnnonce**](#getannonce)       | **GET** /api/v1/annonces/{id}          | Récupérer une annonce par son ID    |
| [**listAnnonces**](#listannonces)   | **POST** /api/v1/annonces/search/list  | Récupérer toutes les annonces       |
| [**putAnnonce**](#putannonce)       | **PUT** /api/v1/annonces/{id}          | Mettre à jour une annonce           |

# **countAnnonces**

> number countAnnonces(annonceSearch)

### Example

```typescript
import { AnnoncesApi, Configuration, AnnonceSearch } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let annonceSearch: AnnonceSearch //

const { status, data } = await apiInstance.countAnnonces(annonceSearch)
```

### Parameters

| Name              | Type              | Description | Notes |
| ----------------- | ----------------- | ----------- | ----- |
| **annonceSearch** | **AnnonceSearch** |             |       |

### Return type

**number**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
| ----------- | ----------- | ---------------- |
| **200**     | Nombre      | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createAnnonce**

> Annonce createAnnonce(annonceBase)

Création d\'une annonce

### Example

```typescript
import { AnnoncesApi, Configuration, AnnonceBase } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let annonceBase: AnnonceBase //

const { status, data } = await apiInstance.createAnnonce(annonceBase)
```

### Parameters

| Name            | Type            | Description | Notes |
| --------------- | --------------- | ----------- | ----- |
| **annonceBase** | **AnnonceBase** |             |       |

### Return type

**Annonce**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details

| Status code | Description               | Response headers |
| ----------- | ------------------------- | ---------------- |
| **201**     | Annonce créée avec succès | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteAnnonce**

> deleteAnnonce()

### Example

```typescript
import { AnnoncesApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let id: string // (default to undefined)

const { status, data } = await apiInstance.deleteAnnonce(id)
```

### Parameters

| Name   | Type         | Description | Notes                 |
| ------ | ------------ | ----------- | --------------------- |
| **id** | [**string**] |             | defaults to undefined |

### Return type

void (empty response body)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: Not defined

### HTTP response details

| Status code | Description       | Response headers |
| ----------- | ----------------- | ---------------- |
| **204**     | Annonce supprimée | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAnnonce**

> Annonce getAnnonce()

### Example

```typescript
import { AnnoncesApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let id: string // (default to undefined)

const { status, data } = await apiInstance.getAnnonce(id)
```

### Parameters

| Name   | Type         | Description | Notes                 |
| ------ | ------------ | ----------- | --------------------- |
| **id** | [**string**] |             | defaults to undefined |

### Return type

**Annonce**

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

# **listAnnonces**

> Annonces listAnnonces(annonceSearch)

Liste paginée des annonces avec filtres et tri possibles

### Example

```typescript
import { AnnoncesApi, Configuration, AnnonceSearch } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let annonceSearch: AnnonceSearch //

const { status, data } = await apiInstance.listAnnonces(annonceSearch)
```

### Parameters

| Name              | Type              | Description | Notes |
| ----------------- | ----------------- | ----------- | ----- |
| **annonceSearch** | **AnnonceSearch** |             |       |

### Return type

**Annonces**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details

| Status code | Description        | Response headers |
| ----------- | ------------------ | ---------------- |
| **200**     | Liste des annonces | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putAnnonce**

> Annonce putAnnonce(annonceWithStatut)

### Example

```typescript
import { AnnoncesApi, Configuration, AnnonceWithStatut } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let id: string // (default to undefined)
let annonceWithStatut: AnnonceWithStatut //

const { status, data } = await apiInstance.putAnnonce(id, annonceWithStatut)
```

### Parameters

| Name                  | Type                  | Description | Notes                 |
| --------------------- | --------------------- | ----------- | --------------------- |
| **annonceWithStatut** | **AnnonceWithStatut** |             |                       |
| **id**                | [**string**]          |             | defaults to undefined |

### Return type

**Annonce**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details

| Status code | Description         | Response headers |
| ----------- | ------------------- | ---------------- |
| **200**     | Annonce mise à jour | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)
