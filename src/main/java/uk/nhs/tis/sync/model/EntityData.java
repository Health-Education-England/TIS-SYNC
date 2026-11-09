package uk.nhs.tis.sync.model;

import lombok.Getter;

/**
 * A model to hold the entity data.
 */
@Getter
public class EntityData {
  private Long entityId;
  private Long otherId;
  private String status;

  public EntityData entityId(Long entityId) {
    this.entityId = entityId;
    return this;
  }

  public EntityData otherId(Long otherId) {
    this.otherId = otherId;
    return this;
  }

  public EntityData status(String status) {
    this.status = status;
    return this;
  }
}
