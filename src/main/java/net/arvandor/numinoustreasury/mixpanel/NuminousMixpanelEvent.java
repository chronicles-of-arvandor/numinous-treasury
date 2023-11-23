package net.arvandor.numinoustreasury.mixpanel;

import java.util.Map;

public interface NuminousMixpanelEvent {

    String getDistinctId();
    String getEventName();
    Map<String, Object> getProps();

}
