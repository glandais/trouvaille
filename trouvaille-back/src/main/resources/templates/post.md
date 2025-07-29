{@io.github.glandais.trouvaille.entity.AnnonceEntity annonce}
{#if creation}
Nouvelle annonce !
{#else}
Mise à jour !
{/if}

@{username} - [**{annonce.titre}**]({frontUrl}/annonces/{annonce.id}) - {annonce.type.label}/{annonce.nature.label}
**{prix}{annonce.prixUnite.label}{#if annonce.periodeLocation != null}/{annonce.periodeLocation.label}{/if}** 
{#if tags}Tags : {#for tag in tags}***{tag}***{#if tag_hasNext} - {/if}{/for}{/if}

{annonce.description}
{#for photo in annonce.photos}

[![photo]({frontUrl}/api/v1/photos/{photo}/256/256)]({frontUrl}/annonces/{annonce.id})
{/for}
