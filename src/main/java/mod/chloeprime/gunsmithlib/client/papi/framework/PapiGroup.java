package mod.chloeprime.gunsmithlib.client.papi.framework;

import com.tacz.guns.client.model.papi.PapiManager;

import java.util.function.BiFunction;

public abstract class PapiGroup<K> implements Papi {
    @SafeVarargs
    public static <K, P extends PapiGroup<K>>
    void register(BiFunction<K, String, P> ctor, String idBase, K... keys) {
        for (K key : keys) {
            var papi = ctor.apply(key, idBase);
            PapiManager.addPapi(papi.id(), papi);
        }
    }

    public final String id() {
        return this.id;
    }

    protected final K key;
    protected final String id;

    protected PapiGroup(K key, String id) {
        this.key = key;
        this.id = id;
    }
}
