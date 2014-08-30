package gg.destiny.app.parsers;

import gg.destiny.app.parsers.extm3u.*;

import java.util.ArrayList;
import java.util.Iterator;

public class extm3uParser
{
  public static int a(ArrayList paramArrayList)
  {
    for (int i = 0; i < paramArrayList.size(); i++)
    {
      Integer localInteger = ((StreamInfo)paramArrayList.get(i)).bandwidth;
      if ((localInteger != null) && (localInteger.intValue() > -1))
      {
        localInteger.intValue();
        return i;
      }
    }
    return -1;
  }

  private static Media a(Media paramMedia, String paramString1, String paramString2)
  {
    if (paramString1.equals("URI"))
    {
      paramMedia.uri = paramString2.replace("\"", "");
      return paramMedia;
    }
    if (paramString1.equals("TYPE"))
    {
      paramMedia.type = paramString2;
      return paramMedia;
    }
    if (paramString1.equals("GROUP-ID"))
    {
      paramMedia.group_id = paramString2.replace("\"", "");
      return paramMedia;
    }
    if (paramString1.equals("LANGUAGE"))
    {
      paramMedia.language = paramString2.replace("\"", "");
      return paramMedia;
    }
    if (paramString1.equals("NAME"))
    {
      paramMedia.name = paramString2.replace("\"", "");
      return paramMedia;
    }
    if (paramString1.equals("DEFAULT"))
    {
      if (paramString2.equals("YES"))
      {
        paramMedia.isDefault = Boolean.valueOf(true);
        return paramMedia;
      }
      paramMedia.isDefault = Boolean.valueOf(false);
      return paramMedia;
    }
    if (paramString1.equals("AUTOSELECT"))
    {
      if (paramString2.equals("YES"))
      {
        paramMedia.autoselect = Boolean.valueOf(true);
        return paramMedia;
      }
      paramMedia.autoselect = Boolean.valueOf(false);
      return paramMedia;
    }
    if (paramString1.equals("FORCED"))
    {
      if (paramString2.equals("YES"))
      {
        paramMedia.forced = Boolean.valueOf(true);
        return paramMedia;
      }
      paramMedia.forced = Boolean.valueOf(false);
      return paramMedia;
    }
    if (paramString1.equals("CHARACTERISTICS"))
    {
      paramMedia.characteristics = paramString2.replace("\"", "");
      return paramMedia;
    }
    Attribute localAttribute = new Attribute();
    localAttribute.a = paramString1;
    localAttribute.b = paramString2;
    paramMedia.attributes.add(localAttribute);
    return paramMedia;
  }

  private static StreamInfo a(StreamInfo paramStreamInfo, String paramString1, String paramString2)
  {
    if (paramString1.equals("BANDWIDTH"))
    {
      paramStreamInfo.bandwidth = Integer.valueOf(Integer.parseInt(paramString2));
      return paramStreamInfo;
    }
    if (paramString1.equals("PROGRAM-ID"))
    {
      paramStreamInfo.program_id = Integer.valueOf(Integer.parseInt(paramString2));
      return paramStreamInfo;
    }
    if (paramString1.equals("CODECS"))
    {
      paramStreamInfo.codecs = paramString2.replace("\"", "");
      return paramStreamInfo;
    }
    if (paramString1.equals("RESOLUTION"))
    {
      paramStreamInfo.resolution = paramString2;
      return paramStreamInfo;
    }
    if (paramString1.equals("AUDIO"))
    {
      paramStreamInfo.audio = paramString2.replace("\"", "");
      return paramStreamInfo;
    }
    if (paramString1.equals("VIDEO"))
    {
      paramStreamInfo.video = paramString2.replace("\"", "");
      return paramStreamInfo;
    }
    if (paramString1.equals("SUBTITLES"))
    {
      paramStreamInfo.subtitles = paramString2.replace("\"", "");
      return paramStreamInfo;
    }
    Attribute localAttribute = new Attribute();
    localAttribute.a = paramString1;
    localAttribute.b = paramString2;
    paramStreamInfo.attributes.add(localAttribute);
    return paramStreamInfo;
  }

  public static extm3u a(String paramString)
  {
    ArrayList<Media> localArrayList1 = new ArrayList<Media>();
    ArrayList localArrayList2 = new ArrayList();
    String localObject = null;
    String[] arrayOfString1 = paramString.split("\n");
    int i = 0;
    while (i < arrayOfString1.length)
    {
      String str3 = arrayOfString1[i];
      if (str3.startsWith("#EXTM3U"));
//      while (true)
//      {
//        attributes++;
//        break;
        if (str3.startsWith("index-"))
        {
          localObject = str3;
        }
        else if (str3.startsWith("#EXT-X-MEDIA:"))
        {
          String str6 = str3.replace("#EXT-X-MEDIA:", "");
          Media localMedia = new Media();
          localMedia.attributes = new ArrayList();
          localMedia.streams = new ArrayList();
          String[] arrayOfString2 = str6.split(",");
          int i2 = arrayOfString2.length;
          for (int i3 = 0; i3 < i2; i3++)
          {
            String[] arrayOfString3 = arrayOfString2[i3].split("=");
            localMedia = a(localMedia, arrayOfString3[0], arrayOfString3[1]);
          }
          localArrayList1.add(localMedia);
        }
        else if (str3.startsWith("#EXT-X-STREAM-INF:"))
        {
          String str4 = str3.replace("#EXT-X-STREAM-INF:", "");
          StreamInfo localStreamInfo2 = new StreamInfo();
          localStreamInfo2.attributes = new ArrayList();
          while (str4.length() != 0)
          {
            int j = str4.indexOf('=');
            String str5 = str4.substring(0, j);
            int k = 44;
            int m = str4.charAt(j + 1);
            int n = 0;
            if (m == 34)
            {
              k = 34;
              j++;
              n = 1;
            }
            int i1 = str4.indexOf(k, j + 1);
            localStreamInfo2 = a(localStreamInfo2, str5, str4.substring(j + 1, i1));
            if (n != 0)
              i1++;
            if (i1 + 1 < str4.length())
              str4 = str4.substring(i1 + 1, str4.length());
            else
              str4 = "";
          }
          if (arrayOfString1[(i + 1)].startsWith("http"))
          {
            localStreamInfo2.url = arrayOfString1[(i + 1)];
            i++;
          }
          localArrayList2.add(localStreamInfo2);
        }
//      }
        i++;
    }
    ArrayList<StreamInfo> localArrayList3 = new ArrayList<StreamInfo>();
    Iterator localIterator = localArrayList2.iterator();
//    label540:
    while (localIterator.hasNext())
    {
      StreamInfo localStreamInfo1 = (StreamInfo)localIterator.next();
      String str2 = null;
      if (localStreamInfo1.video != null)
        str2 = localStreamInfo1.video;
//      while (true)
//      {
        if (str2 == null || !a(localStreamInfo1, str2, localArrayList1))
//          break label540;
          localArrayList3.add(localStreamInfo1);
//        break;
//        if (localStreamInfo1.audio != null)
//        {
//          str2 = localStreamInfo1.audio;
//        }
//        else
//        {
//          str2 = null;
//          if (localStreamInfo1.subtitles != null)
//            str2 = localStreamInfo1.subtitles;
//        }
//      }
    }
    extm3u localextm3u = new extm3u();
    localextm3u.media = localArrayList1;
    localextm3u.streams = localArrayList3;
    localextm3u.c = localObject;
    return localextm3u;
  }

  private static boolean a(StreamInfo paramStreamInfo, String paramString, ArrayList paramArrayList)
  {
    Iterator localIterator = paramArrayList.iterator();
    while (localIterator.hasNext())
    {
      Media localMedia = (Media)localIterator.next();
      if (localMedia.group_id != null && paramString.equals(localMedia.group_id)) {
        localMedia.streams.add(paramStreamInfo);
        return true;
      }
    }
    return false;
  }

  public static boolean a(extm3u paramextm3u, String paramString)
  {
    Iterator localIterator = paramextm3u.media.iterator();
    while (localIterator.hasNext())
    {
      Media localMedia = (Media)localIterator.next();
      if ((localMedia.name != null) && (localMedia.name.equals(paramString)))
        return true;
    }
    return false;
  }
}
