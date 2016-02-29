package com.sbbic.net;

import android.app.Activity;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import sbbic.com.androidlib.R;

/**
 * Created by God on 2016/2/29.
 */
public class URLManager {
    public static URLData findURL(final Activity activity, final String findkey) {
        XmlResourceParser parser = activity.getApplication().getResources().getXml(R.xml.urls);
        int eventCode;
        try {
            eventCode= parser.getEventType();
            while (eventCode != XmlPullParser.END_DOCUMENT) {
                switch (eventCode) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("Node".equals(parser.getName())) {
                            final String key = parser.getAttributeValue(null, "Key");
                            if (key.trim().equals(findkey)) {
                                URLData urlData = new URLData();
                                urlData.setKey(findkey);
                                urlData.setUrl(parser.getAttributeValue(null, "Url"));
                                urlData.setNetType(parser.getAttributeValue(null, "NetType"));
                                urlData.setExpires(Long.parseLong(parser.getAttributeValue(null, "Expires")));
                                urlData.setMockClass(parser.getAttributeValue(null,"MockClass"));
                                return urlData;
                            }
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;

                }
                eventCode=parser.next();
            }
        }catch (XmlPullParserException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
