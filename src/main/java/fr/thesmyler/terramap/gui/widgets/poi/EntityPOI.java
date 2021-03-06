package fr.thesmyler.terramap.gui.widgets.poi;

import fr.thesmyler.terramap.TerramapUtils;
import io.github.terra121.projection.GeographicProjection;
import net.minecraft.entity.Entity;

//TODO Distinguish different entity types
public class EntityPOI extends PointOfInterest {

	protected Entity entity;

	public EntityPOI(Entity e) {
		this.entity = e;
	}

	public void updatePosition(GeographicProjection projection) {
		double x = this.entity.posX;
		double z = this.entity.posZ;
		double coords[] = TerramapUtils.toGeo(projection, x, z);
		this.longitude = coords[0];
		this.latitude = coords[1];
	}
	
	public Entity getEntity() {
		return this.entity;
	}

	@Override
	public String getDisplayName() {
		return this.getEntity().getDisplayName().getFormattedText();
	}

	@Override
	public int getWidth() {
		return 10;
	}

	@Override
	public int getHeight() {
		return 10;
	}

	@Override
	public int getXOffset() {
		return -5;
	}

	@Override
	public int getYOffset() {
		return -5;
	}
}
