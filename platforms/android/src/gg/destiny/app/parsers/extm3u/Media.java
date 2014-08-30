package gg.destiny.app.parsers.extm3u;

import java.util.ArrayList;

public class Media
{
  public String uri;
  public String type;
  public String group_id;
  public String language;
  public String name;
  public Boolean isDefault;
  public Boolean autoselect;
  public Boolean forced;
  public String characteristics;
  public ArrayList attributes;
  public ArrayList<StreamInfo> streams;
}
