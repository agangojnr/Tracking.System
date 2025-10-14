
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_user_level")
public class UserLevel extends ExtendedModel {

    private Long userid;

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    private Long levelid;

    public Long getLevelid() {
        return levelid;
    }

    public void setLevelid(Long levelid) {
        this.levelid = levelid;
    }

    private Long levelgroupid;

    public Long getLevelgroupid() {
        return levelgroupid;
    }

    public void setLevelgroupid(Long levelgroupid) {
        this.levelgroupid = levelgroupid;
    }

}
