package tcy.addon.atrihack.mixins;

import meteordevelopment.meteorclient.renderer.Mesh;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import tcy.addon.atrihack.utils.notifications.MeshAccessor;

@Mixin(Mesh.class)
public interface MeshMixin extends MeshAccessor {
	@Accessor(remap = false)
	int getIndicesCount();

	@Accessor(remap = false)
	long getIndicesPointer();

	@Accessor(remap = false)
	void setIndicesCount(int count);
}
