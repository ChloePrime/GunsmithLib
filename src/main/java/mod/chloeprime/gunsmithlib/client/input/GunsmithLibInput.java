package mod.chloeprime.gunsmithlib.client.input;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import javax.annotation.Nonnull;

@EventBusSubscriber(Dist.CLIENT)
public class GunsmithLibInput {
    public static final String CATEGORY = "key.category.%s".formatted(GunsmithLib.MOD_ID);

    public enum KeyConflictContexts implements IKeyConflictContext {
        IN_GAME_CONCURRENT {
            @Override
            public boolean isActive() {
                return KeyConflictContext.IN_GAME.isActive();
            }

            @Override
            public boolean conflicts(@Nonnull IKeyConflictContext other) {
                return false;
            }
        },

        UNIVERSAL_CONCURRENT {
            @Override
            public boolean isActive() {
                return KeyConflictContext.UNIVERSAL.isActive();
            }

            @Override
            public boolean conflicts(@Nonnull IKeyConflictContext other) {
                return false;
            }
        }
    }

    @SubscribeEvent
    public static void onRegisteringKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BallisticComputerKey.KEY_MAPPING);
        event.register(SwitchPartOrAmmoTypeKey.KEY_MAPPING);
    }
}
