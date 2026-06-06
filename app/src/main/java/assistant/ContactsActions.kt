package assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast

class ContactsActions(
    private val context: Context
) {

    fun openContacts() {

        try {

            val intent = Intent(
                Intent.ACTION_VIEW,
                ContactsContract.Contacts.CONTENT_URI
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                context,
                "Unable to open contacts",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun callNumber(
        phoneNumber: String
    ) {

        try {

            val intent = Intent(
                Intent.ACTION_DIAL
            )

            intent.data =
                Uri.parse(
                    "tel:$phoneNumber"
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                context,
                "Unable to start call",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun callContact(
        contactName: String
    ) {

        val cursor =
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )

        cursor?.use {

            while (it.moveToNext()) {

                val name =
                    it.getString(
                        it.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        )
                    )

                if (
                    name.equals(
                        contactName,
                        ignoreCase = true
                    )
                ) {

                    val number =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )

                    callNumber(number)
                    return
                }
            }
        }

        Toast.makeText(
            context,
            "Contact not found: $contactName",
            Toast.LENGTH_SHORT
        ).show()
    }
}