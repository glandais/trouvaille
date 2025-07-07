{@io.github.glandais.trouvaille.entity.AnnonceEntity annonce}
{#if creation}
Nouvelle annonce !
{#else}
Mise à jour !
{/if}

{annonce.type.label}/{annonce.nature.label} - @{username} - {annonce.prix}€{#if annonce.periodeLocation != null}/{annonce.periodeLocation.label}{/if} - [**{annonce.titre}**]({frontUrl}/annonces/{annonce.id}) - {annonce.photos.size} photo(s)

{annonce.description}

{#for photo in annonce.photos}![photo]({frontUrl}/api/v1/photos/{photo}/thumb){/for}
