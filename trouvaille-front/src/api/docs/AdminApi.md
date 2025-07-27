# AdminApi

All URIs are relative to _http://localhost_

| Method                                  | HTTP request                               | Description                                                                |
| --------------------------------------- | ------------------------------------------ | -------------------------------------------------------------------------- |
| [**createTag**](#createtag)             | **POST** /api/v1/admin/tags                | Créer un nouveau tag (admin uniquement)                                    |
| [**listTags**](#listtags)               | **GET** /api/v1/admin/tags                 | Lister tous les tags (admin uniquement)                                    |
| [**listUsers**](#listusers)             | **GET** /api/v1/admin/users                | Lister tous les utilisateurs (admin uniquement)                            |
| [**updateTag**](#updatetag)             | **PUT** /api/v1/admin/tags/{tagId}         | Modifier un tag (admin uniquement)                                         |
| [**updateUserAdmin**](#updateuseradmin) | **PUT** /api/v1/admin/users/{userId}/admin | Modifier le statut administrateur d\&#39;un utilisateur (admin uniquement) |

# **createTag**

> Tag createTag(tagCreateUpdate)

### Example

```typescript
import { AdminApi, Configuration, TagCreateUpdate } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AdminApi(configuration)

let tagCreateUpdate: TagCreateUpdate //

const { status, data } = await apiInstance.createTag(tagCreateUpdate)
```

### Parameters

| Name                | Type                | Description | Notes |
| ------------------- | ------------------- | ----------- | ----- |
| **tagCreateUpdate** | **TagCreateUpdate** |             |       |

### Return type

**Tag**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details

| Status code | Description                                 | Response headers |
| ----------- | ------------------------------------------- | ---------------- |
| **201**     | Tag créé avec succès                        | -                |
| **400**     | Données invalides                           | -                |
| **403**     | Accès refusé - droits administrateur requis | -                |
| **409**     | Un tag avec ce nom existe déjà              | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listTags**

> Array<Tag> listTags()

### Example

```typescript
import { AdminApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AdminApi(configuration)

const { status, data } = await apiInstance.listTags()
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

| Status code | Description                                 | Response headers |
| ----------- | ------------------------------------------- | ---------------- |
| **200**     | Liste des tags                              | -                |
| **403**     | Accès refusé - droits administrateur requis | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listUsers**

> Users listUsers()

### Example

```typescript
import { AdminApi, Configuration } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AdminApi(configuration)

let page: number // (optional) (default to 1)
let limit: number // (optional) (default to 20)
let search: string // (optional) (default to undefined)

const { status, data } = await apiInstance.listUsers(page, limit, search)
```

### Parameters

| Name       | Type         | Description | Notes                            |
| ---------- | ------------ | ----------- | -------------------------------- |
| **page**   | [**number**] |             | (optional) defaults to 1         |
| **limit**  | [**number**] |             | (optional) defaults to 20        |
| **search** | [**string**] |             | (optional) defaults to undefined |

### Return type

**Users**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details

| Status code | Description                                 | Response headers |
| ----------- | ------------------------------------------- | ---------------- |
| **200**     | Liste des utilisateurs                      | -                |
| **403**     | Accès refusé - droits administrateur requis | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateTag**

> Tag updateTag(tagCreateUpdate)

### Example

```typescript
import { AdminApi, Configuration, TagCreateUpdate } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AdminApi(configuration)

let tagId: string // (default to undefined)
let tagCreateUpdate: TagCreateUpdate //

const { status, data } = await apiInstance.updateTag(tagId, tagCreateUpdate)
```

### Parameters

| Name                | Type                | Description | Notes                 |
| ------------------- | ------------------- | ----------- | --------------------- |
| **tagCreateUpdate** | **TagCreateUpdate** |             |                       |
| **tagId**           | [**string**]        |             | defaults to undefined |

### Return type

**Tag**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details

| Status code | Description                                 | Response headers |
| ----------- | ------------------------------------------- | ---------------- |
| **200**     | Tag modifié avec succès                     | -                |
| **400**     | Données invalides                           | -                |
| **403**     | Accès refusé - droits administrateur requis | -                |
| **404**     | Tag non trouvé                              | -                |
| **409**     | Un tag avec ce nom existe déjà              | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateUserAdmin**

> Utilisateur updateUserAdmin(userAdminUpdate)

### Example

```typescript
import { AdminApi, Configuration, UserAdminUpdate } from '@trouvaille/api'

const configuration = new Configuration()
const apiInstance = new AdminApi(configuration)

let userId: string // (default to undefined)
let userAdminUpdate: UserAdminUpdate //

const { status, data } = await apiInstance.updateUserAdmin(userId, userAdminUpdate)
```

### Parameters

| Name                | Type                | Description | Notes                 |
| ------------------- | ------------------- | ----------- | --------------------- |
| **userAdminUpdate** | **UserAdminUpdate** |             |                       |
| **userId**          | [**string**]        |             | defaults to undefined |

### Return type

**Utilisateur**

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details

| Status code | Description                                 | Response headers |
| ----------- | ------------------------------------------- | ---------------- |
| **200**     | Statut administrateur mis à jour            | -                |
| **403**     | Accès refusé - droits administrateur requis | -                |
| **404**     | Utilisateur non trouvé                      | -                |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)
