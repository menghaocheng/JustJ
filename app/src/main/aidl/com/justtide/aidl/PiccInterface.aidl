// PiccInterface.aidl
package com.justtide.aidl;

import com.justtide.aidl.ContactlessCard;

interface PiccInterface {
	void getContactlessCard(int searchResult, in ContactlessCard contactlessCard);
}

