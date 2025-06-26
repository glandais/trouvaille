# AnnoncesApi

All URIs are relative to _http://localhost_

| Method                              | HTTP request                     | Description                      |
| ----------------------------------- | -------------------------------- | -------------------------------- |
| [**createAnnonce**](#createannonce) | **POST** /api/v1/annonces        | Créer une nouvelle annonce       |
| [**deleteAnnonce**](#deleteannonce) | **DELETE** /api/v1/annonces/{id} | Supprimer une annonce            |
| [**getAnnonce**](#getannonce)       | **GET** /api/v1/annonces/{id}    | Récupérer une annonce par son ID |
| [**listAnnonces**](#listannonces)   | **GET** /api/v1/annonces         | Récupérer toutes les annonces    |
| [**putAnnonce**](#putannonce)       | **PUT** /api/v1/annonces/{id}    | Mettre à jour une annonce        |

# **createAnnonce**

> Annonce createAnnonce(annonceCreate)

Création d\'une annonce

### Example

```typescript
import { AnnoncesApi, Configuration, AnnonceCreate } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let annonceCreate: AnnonceCreate //

const { status, data } = await apiInstance.createAnnonce(annonceCreate)
```

### Parameters

| Name              | Type              | Description | Notes |
| ----------------- | ----------------- | ----------- | ----- |
| **annonceCreate** | **AnnonceCreate** |             |       |

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
| **400**     | Données invalides         | -                |

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
- **Accept**: application/json

### HTTP response details

| Status code | Description         | Response headers |
| ----------- | ------------------- | ---------------- |
| **204**     | Annonce supprimée   | -                |
| **404**     | Annonce non trouvée | -                |

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
| **404**     | Annonce non trouvée       | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listAnnonces**

> Annonces listAnnonces()

Liste paginée des annonces avec filtres et tri possibles

### Example

```typescript
import { AnnoncesApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let type: AnnonceType //Type d\'annonce (optional) (default to undefined)
let statut: AnnonceStatut //Statut de l\'annonce (optional) (default to undefined)
let nature: AnnonceNature //Nature de l\'annonce (optional) (default to undefined)
let page: number //Numéro de page (optional) (default to 1)
let limit: number //Nombre d\'éléments par page (optional) (default to 20)
let search: string //Recherche textuelle dans titre et description (optional) (default to undefined)
let userId: string //Recherche par utilisateur (optional) (default to undefined)
let prixMin: number //Prix minimum (optional) (default to undefined)
let prixMax: number //Prix maximum (optional) (default to undefined)
let latitude: number //Latitude pour le tri par distance (optional) (default to undefined)
let longitude: number //Longitude pour le tri par distance (optional) (default to undefined)
let distanceMax: number //Distance maximale en kilomètres (nécessite lat/lon) (optional) (default to undefined)
let sortBy: 'date_creation' | 'date_modification' | 'prix' | 'titre' | 'distance' //Champ de tri (optional) (default to 'date_creation')
let sortOrder: 'asc' | 'desc' //Ordre de tri (optional) (default to 'desc')

const { status, data } = await apiInstance.listAnnonces(
  type,
  statut,
  nature,
  page,
  limit,
  search,
  userId,
  prixMin,
  prixMax,
  latitude,
  longitude,
  distanceMax,
  sortBy,
  sortOrder,
)
```

### Parameters

| Name            | Type                         | Description                                                       | Notes                            |
| --------------- | ---------------------------- | ----------------------------------------------------------------- | -------------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------ | -------------------------------------- |
| **type**        | **AnnonceType**              | Type d\&#39;annonce                                               | (optional) defaults to undefined |
| **statut**      | **AnnonceStatut**            | Statut de l\&#39;annonce                                          | (optional) defaults to undefined |
| **nature**      | **AnnonceNature**            | Nature de l\&#39;annonce                                          | (optional) defaults to undefined |
| **page**        | [**number**]                 | Numéro de page                                                    | (optional) defaults to 1         |
| **limit**       | [**number**]                 | Nombre d\&#39;éléments par page                                   | (optional) defaults to 20        |
| **search**      | [**string**]                 | Recherche textuelle dans titre et description                     | (optional) defaults to undefined |
| **userId**      | [**string**]                 | Recherche par utilisateur                                         | (optional) defaults to undefined |
| **prixMin**     | [**number**]                 | Prix minimum                                                      | (optional) defaults to undefined |
| **prixMax**     | [**number**]                 | Prix maximum                                                      | (optional) defaults to undefined |
| **latitude**    | [**number**]                 | Latitude pour le tri par distance                                 | (optional) defaults to undefined |
| **longitude**   | [**number**]                 | Longitude pour le tri par distance                                | (optional) defaults to undefined |
| **distanceMax** | [**number**]                 | Distance maximale en kilomètres (nécessite lat/lon)               | (optional) defaults to undefined |
| **sortBy**      | [\*\*&#39;date_creation&#39; | &#39;date_modification&#39;                                       | &#39;prix&#39;                   | &#39;titre&#39;               | &#39;distance&#39;**]**Array<&#39;date_creation&#39; &#124; &#39;date_modification&#39; &#124; &#39;prix&#39; &#124; &#39;titre&#39; &#124; &#39;distance&#39;>\*\* | Champ de tri | (optional) defaults to 'date_creation' |
| **sortOrder**   | [\*\*&#39;asc&#39;           | &#39;desc&#39;**]**Array<&#39;asc&#39; &#124; &#39;desc&#39;>\*\* | Ordre de tri                     | (optional) defaults to 'desc' |

### Return type

**Annonces**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details

| Status code | Description        | Response headers |
| ----------- | ------------------ | ---------------- |
| **200**     | Liste des annonces | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putAnnonce**

> Annonce putAnnonce(annonceUpdate)

### Example

```typescript
import { AnnoncesApi, Configuration, AnnonceUpdate } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AnnoncesApi(configuration)

let id: string // (default to undefined)
let annonceUpdate: AnnonceUpdate //

const { status, data } = await apiInstance.putAnnonce(id, annonceUpdate)
```

### Parameters

| Name              | Type              | Description | Notes                 |
| ----------------- | ----------------- | ----------- | --------------------- |
| **annonceUpdate** | **AnnonceUpdate** |             |                       |
| **id**            | [**string**]      |             | defaults to undefined |

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
| **404**     | Annonce non trouvée | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)
