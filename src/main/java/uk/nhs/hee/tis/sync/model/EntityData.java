package uk.nhs.hee.tis.sync.model;

public class EntityData {
  private Long entityId;
  private Long otherId;

  public Long getEntityId() {
    return entityId;
  }

  public EntityData entityId(Long entityId) {
    this.entityId = entityId;
    return this;
  }

  public Long getOtherId() {
    return otherId;
  }

  public EntityData otherId(Long otherId) {
    this.otherId = otherId;
    return this;
  }
}
