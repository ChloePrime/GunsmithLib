(() => {
    const AMMO_I = id => Ingredient.of(`tacz:ammo[minecraft:custom_data={AmmoId:"${id}"}]`);
    const AMMO   = id => Item.of(`tacz:ammo[minecraft:custom_data={AmmoId:"${id}"}]`);

    ServerEvents.recipes(e => {
        e.recipes.create
            .mixing([AMMO('tacz:50bmg')], [AMMO_I('tacz:556x45'), AMMO_I('tacz:50ae')], 100)
            .superheated()
            .id('gunsmithlib:item_particle_test');
    });
})();