package deltaiot.client;

import deltaiot.services.LinkSettings;

import java.util.List;

public interface Effector {

    public void setMoteSettings(int moteId, List<LinkSettings> linkSettings);

    public void setDefaultConfiguration();
}
