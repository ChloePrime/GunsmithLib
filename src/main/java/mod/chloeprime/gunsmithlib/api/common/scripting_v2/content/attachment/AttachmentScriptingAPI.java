package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.attachment;

import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import mod.chloeprime.gunsmithlib.api.common.GunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.GunsmithLibCommonScriptExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ShooterStates;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.SyncedData;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.ItemSyncedDataImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaTable;

/**
 * 配件脚本的 API。
 *
 * @param gun                    配件所安装的枪械的 API
 * @param attachment_type_object 配件类型的枚举对象
 * @param attachment_info        配件数据
 * @param script_params          配件 data 中指定的脚本参数
 * @param privateData            配件的不面向脚本公开的数据
 * @since 6.2
 */
@SuppressWarnings("unused")
public record AttachmentScriptingAPI(
        ModernKineticGunScriptAPI gun,

        @ApiStatus.Experimental
        AttachmentType attachment_type_object,

        @ApiStatus.Experimental
        AttachmentInfo attachment_info,

        LuaTable script_params,
        PrivateData privateData
) {
    /**
     * 获取配件类型的名字。
     *
     * @return 配件类型的名字。
     * @see AttachmentType 以参阅可能的返回值。
     */
    public String attachment_type() {
        return attachment_type_object().name();
    }


    /**
     * 获取配件物品。
     * 这是一个面向高级用户的 API。存取数据请使用 {@link #synced_data()}
     *
     * @return 配件物品
     */
    @ApiStatus.Experimental
    public ItemStack attachment_stack() {
        return attachment_info().attachmentStack();
    }

    /**
     * 获取配件 Item。
     * 这是一个面向高级用户的 API。
     *
     * @return 配件 Item
     */
    @ApiStatus.Experimental
    public IAttachment attachment_item_interface() {
        return attachment_info().attachmentItem();
    }

    /**
     * 获取配件 ID。
     *
     * @return 配件 ID
     */
    public ResourceLocation attachment_id() {
        return attachment_info().attachmentId();
    }

    /**
     * 获取配件的 index。
     *
     * @return 配件的 index
     */
    public CommonAttachmentIndex attachment_index() {
        return attachment_info().index();
    }


    /**
     * 获取射手的各种状态。
     * 这个方法在逻辑脚本中永远不会返回 {@code nil}。
     *
     * @return 获取射手的各种状态的接口
     * @see GunsmithLibCommonScriptExtension#shooter_states()
     */
    public ShooterStates shooter_states() {
        return ((GunScriptAPIExtension) (Object) gun()).gunsmithlib_extension().shooter_states();
    }

    /**
     * 获取配件的同步数据接口。
     * 在服务端（逻辑脚本）中反馈的是可写入的，在客户端中返回的是只读的，不可写入。
     *
     * @return 基于配件物品 NBT 的同步数据接口
     * @see SyncedData 逻辑机调用时返回的，可写入的接口
     */
    public SyncedData synced_data() {
        return new ItemSyncedDataImpl(attachment_info().attachmentStack(), false, this::updateAttachment);
    }


    // 小写驼峰风格的 API


    /**
     * 获取枪械的脚本 API。
     * {@link #gun()} 的别名。
     *
     * @return 枪械的脚本 API
     */
    public ModernKineticGunScriptAPI getGunApi() {
        return gun();
    }

    /**
     * 获取配件类型。
     * {@link #attachment_type_object()} 的别名。
     * 这是一个面向高级用户的 API。
     *
     * @return 配件类型。
     * @see AttachmentType 以参阅可能的返回值。
     */
    @ApiStatus.Experimental
    public AttachmentType getAttachmentTypeObject() {
        return attachment_type_object();
    }

    /**
     * 获取配件的数据。
     * {@link #attachment_info()} 的别名。
     * 这是一个面向高级用户的 API。
     *
     * @return 配件配件的数据。
     */
    @ApiStatus.Experimental
    public AttachmentInfo getAttachmentInfo() {
        return attachment_info();
    }

    /**
     * 获取配件 data 中指定的脚本参数。
     * {@link #script_params()} 的别名。
     *
     * @return data 文件中指定的脚本参数。
     */
    public LuaTable getScriptParams() {
        return script_params();
    }


    /**
     * 获取配件类型的名字。
     * {@link #attachment_type()} 的别名。
     *
     * @return 配件类型的名字。
     * @see AttachmentType 以参阅可能的返回值。
     */
    public String getAttachmentType() {
        return attachment_type();
    }


    /**
     * 获取配件物品。
     * {@link #attachment_stack()} 的别名。
     * 这是一个面向高级用户的 API。存取数据请使用 {@link #synced_data()}
     *
     * @return 配件物品
     */
    @ApiStatus.Experimental
    public ItemStack getAttachmentItemStack() {
        return attachment_stack();
    }

    /**
     * 获取配件 Item。
     * {@link #attachment_item_interface()} 的别名。
     * 这是一个面向高级用户的 API。
     *
     * @return 配件 Item
     */
    @ApiStatus.Experimental
    public IAttachment getIAttachment() {
        return attachment_item_interface();
    }

    /**
     * 获取配件 ID。
     * {@link #attachment_id()} 的别名。
     *
     * @return 配件 ID
     */
    public ResourceLocation getAttachmentId() {
        return attachment_id();
    }

    /**
     * 获取配件的 index。
     * {@link #attachment_index()} 的别名。
     *
     * @return 配件的 index
     */
    public CommonAttachmentIndex getAttachmentIndex() {
        return attachment_index();
    }


    /**
     * 获取射手的各种状态。
     * 这个方法在逻辑脚本中永远不会返回 {@code nil}。
     * {@link #shooter_states()} 的别名。
     *
     * @return 获取射手的各种状态的接口
     * @see GunsmithLibCommonScriptExtension#shooter_states()
     */
    public ShooterStates getShooterStates() {
        return shooter_states();
    }

    /**
     * 获取配件的同步数据接口。
     * 在服务端（逻辑脚本）中反馈的是可写入的，在客户端中返回的是只读的，不可写入。
     * {@link #synced_data()} 的别名。
     *
     * @return 基于配件物品 NBT 的同步数据接口
     * @see SyncedData 逻辑机调用时返回的，可写入的接口
     */
    public SyncedData getSyncedData() {
        return synced_data();
    }


    @ApiStatus.Internal
    public AttachmentScriptingAPI(
            ModernKineticGunScriptAPI gun,
            AttachmentType attachmentType,
            AttachmentInfo attachmentInfo,
            LuaTable scriptParams
    ) {
        this(gun, attachmentType, attachmentInfo, scriptParams, new PrivateData(gun.getShooter()));
    }

    private record PrivateData(
            LivingEntity shooter
    ) {
    }

    private void updateAttachment(ItemStack newAttachment) {
        var gunNbt = gun.getItemStack().getOrCreateTag();
        var nbtKey = GunItemDataAccessor.GUN_ATTACHMENT_BASE + attachment_type_object().name();
        var serializedAttachment = new CompoundTag();
        newAttachment.save(serializedAttachment);
        gunNbt.put(nbtKey, serializedAttachment);
    }
}
