package mod.chloeprime.gunsmithlib.data;

import mod.chloeprime.gunsmithlib.data.client.GunsmithLibBlockStateProvider;
import mod.chloeprime.gunsmithlib.data.common.GunsmithLibBlockTagProvider;
import mod.chloeprime.gunsmithlib.data.common.GunsmithLibItemTagProvider;
import mod.chloeprime.gunsmithlib.data.common.GunsmithLibLootProvider;
import mod.chloeprime.gunsmithlib.data.common.GunsmithLibRecipeProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GunsmithLibDatagen {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        if (event.includeServer()) {
            var bp = new GunsmithLibBlockTagProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());
            generator.addProvider(true, bp);
            generator.addProvider(true, new GunsmithLibItemTagProvider(generator.getPackOutput(), event.getLookupProvider(), bp.contentsGetter(), event.getExistingFileHelper()));
            generator.addProvider(true, new GunsmithLibLootProvider(generator.getPackOutput()));
            generator.addProvider(true, new GunsmithLibRecipeProvider(generator.getPackOutput()));
        }
        if (event.includeClient()) {
            generator.addProvider(true, new GunsmithLibBlockStateProvider(generator.getPackOutput(), event.getExistingFileHelper()));
        }
    }
}
