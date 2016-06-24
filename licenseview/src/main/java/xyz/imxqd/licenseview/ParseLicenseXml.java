/*
 * Copyright 2013 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.imxqd.licenseview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;

public class ParseLicenseXml {

	private static final String TAG_ROOT = "licenses";
	private static final String TAG_CHILD = "license";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_URL = "url";

	public static List<License> Parse(XmlResourceParser parser)
			throws XmlPullParserException, IOException {
		List<License> licenses = new ArrayList<License>();
		int event = parser.getEventType();

		String name = null;
		String url = null;
		String license = null;

		while (event != XmlResourceParser.END_DOCUMENT) {
			if (event == XmlResourceParser.START_TAG) {
				if (!parser.getName().equals(TAG_ROOT)
						&& !parser.getName().equals(TAG_CHILD))
					throw new XmlPullParserException(
							"Error in xml: tag isn't '" + TAG_ROOT + "' or '"
									+ TAG_CHILD + "' at line:"
									+ parser.getLineNumber());
				name = parser.getAttributeValue(null, ATTR_NAME);
				url = parser.getAttributeValue(null, ATTR_URL);
			} else if (event == XmlResourceParser.TEXT) {
				license = parser.getText();
			} else if (event == XmlResourceParser.END_TAG) {
				if (name != null && license != null
						&& !parser.getName().equals(TAG_ROOT)) {
					licenses.add(new License(name, url, license ));
				} else if (name == null) {
					throw new XmlPullParserException(
							"Error in xml: doesn't contain a 'name' at line:"
									+ parser.getLineNumber());
				}else if (license == null){
					throw new XmlPullParserException(
							"Error in xml: doesn't contain a 'license text' at line:"
									+ parser.getLineNumber());
				}
			}
			event = parser.next();
		}
		parser.close();
		return licenses;
	}
}
