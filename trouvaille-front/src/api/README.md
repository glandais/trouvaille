## @trouvaille/api@1.0.0

This generator creates TypeScript/JavaScript client that utilizes [axios](https://github.com/axios/axios). The generated Node module can be used in the following environments:

Environment
* Node.js
* Webpack
* Browserify

Language level
* ES5 - you must have a Promises/A+ library installed
* ES6

Module system
* CommonJS
* ES6 module system

It can be used in both TypeScript and JavaScript. In TypeScript, the definition will be automatically resolved via `package.json`. ([Reference](https://www.typescriptlang.org/docs/handbook/declaration-files/consumption.html))

### Building

To build and compile the typescript sources to javascript use:
```
npm install
npm run build
```

### Publishing

First build the package then run `npm publish`

### Consuming

navigate to the folder of your consuming project and run one of the following commands.

_published:_

```
npm install @trouvaille/api@1.0.0 --save
```

_unPublished (not recommended):_

```
npm install PATH_TO_GENERATED_PACKAGE --save
```

### Documentation for API Endpoints

All URIs are relative to *http://localhost*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AnnoncesApi* | [**createAnnonce**](docs/AnnoncesApi.md#createannonce) | **POST** /api/v1/annonces | Créer une nouvelle annonce
*AnnoncesApi* | [**deleteAnnonce**](docs/AnnoncesApi.md#deleteannonce) | **DELETE** /api/v1/annonces/{id} | Supprimer une annonce
*AnnoncesApi* | [**getAnnonce**](docs/AnnoncesApi.md#getannonce) | **GET** /api/v1/annonces/{id} | Récupérer une annonce par son ID
*AnnoncesApi* | [**listAnnonces**](docs/AnnoncesApi.md#listannonces) | **GET** /api/v1/annonces | Récupérer toutes les annonces
*AnnoncesApi* | [**putAnnonce**](docs/AnnoncesApi.md#putannonce) | **PUT** /api/v1/annonces/{id} | Mettre à jour une annonce
*AuthentificationApi* | [**exchangeOAuthToken**](docs/AuthentificationApi.md#exchangeoauthtoken) | **POST** /api/v1/auth/token | Échanger le code OAuth pour un token
*PhotosApi* | [**createPhoto**](docs/PhotosApi.md#createphoto) | **POST** /api/v1/annonces/photos | Ajouter une photo
*PhotosApi* | [**deletePhoto**](docs/PhotosApi.md#deletephoto) | **DELETE** /api/v1/annonces/photos/{photoId} | Supprimer une photo


### Documentation For Models

 - [Annonce](docs/Annonce.md)
 - [AnnonceBase](docs/AnnonceBase.md)
 - [AnnonceCreate](docs/AnnonceCreate.md)
 - [AnnonceList](docs/AnnonceList.md)
 - [AnnonceNature](docs/AnnonceNature.md)
 - [AnnonceStatut](docs/AnnonceStatut.md)
 - [AnnonceType](docs/AnnonceType.md)
 - [AnnonceUpdate](docs/AnnonceUpdate.md)
 - [Annonces](docs/Annonces.md)
 - [Coordinates](docs/Coordinates.md)
 - [ErrorDetailsInner](docs/ErrorDetailsInner.md)
 - [ModelError](docs/ModelError.md)
 - [OAuthTokenRequest](docs/OAuthTokenRequest.md)
 - [OAuthTokenResponse](docs/OAuthTokenResponse.md)
 - [Pagination](docs/Pagination.md)
 - [PeriodeLocation](docs/PeriodeLocation.md)
 - [Utilisateur](docs/Utilisateur.md)


<a id="documentation-for-authorization"></a>
## Documentation For Authorization


Authentication schemes defined for the API:
<a id="BearerAuth"></a>
### BearerAuth

- **Type**: Bearer authentication (JWT)

