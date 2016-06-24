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
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class LicenseView extends ScrollView {

	LinearLayout mContainer;

	public LicenseView(Context context) {
		super(context);
		init();
	}

	public LicenseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LicenseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mContainer = new LinearLayout(getContext());
		mContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mContainer.setOrientation(LinearLayout.VERTICAL);
		addView(mContainer);
	}

	public void setLicenses(int id) {
		List<License> licenses = null;
		try {
			licenses = ParseLicenseXml.Parse(getResources()
                    .getXml(id));
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View child;
		TextView title;
		TextView url;
		TextView license;

		for (License i : licenses) {
			child = inflater.inflate(R.layout.license_layout, null);
			title = (TextView) child.findViewById(R.id.license_title);
			url = (TextView) child.findViewById(R.id.license_describe);
			license = (TextView) child.findViewById(R.id.license_license);
			title.setText(i.getName());
			url.setText(i.getUrl());
			license.setText(i.getLicense());
			mContainer.addView(child);
		}

	}

}
