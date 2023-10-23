package com.b1nar10.ml_face_recognition_tutorial.ui

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.b1nar10.ml_face_recognition_tutorial.R
import java.io.File

/**
 * A dialog fragment to input details for a recognized face
 */
class InputDetailsDialogFragment : DialogFragment() {

    interface InputDetailsListener {
        fun onDetailsEntered(id: String, name: String)
        fun cancel()
    }

    private var listener: InputDetailsListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater
            val view = inflater.inflate(R.layout.dialog_person_identification_detail, null)

            val idEditText: EditText = view.findViewById(R.id.idEt)
            val nameEditText: EditText = view.findViewById(R.id.nameEt)
            val faceImageView: ImageView = view.findViewById(R.id.faceImageView)
            val saveButton: Button = view.findViewById(R.id.saveBt)
            val cancelButton: Button = view.findViewById(R.id.cancelBt)

            // Display the bitmap (face image) on the ImageView
            faceImageView.setImageBitmap(getBitmap())

            // Handle save button click
            saveButton.setOnClickListener {
                deleteBitmapFile()
                val id = idEditText.text.toString()
                val name = nameEditText.text.toString()

                // Check if both fields are filled
                if (id.isNotBlank() && name.isNotBlank()) {
                    listener?.onDetailsEntered(id, name)
                    dismiss()
                } else {
                    Toast.makeText(
                        context,
                        "Both ID and Name are required!",
                        Toast.LENGTH_SHORT
                    ).show()

                    listener?.cancel()
                }
            }

            // Handle cancel button click
            cancelButton.setOnClickListener {
                deleteBitmapFile()
                dismiss()
                listener?.cancel()
            }

            builder.setView(view)
                .setTitle("Enter Details")
                .create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        deleteBitmapFile()
    }

    /**
     * Set the listener for input events
     */
    fun setListener(listener: InputDetailsListener) {
        this.listener = listener
    }

    /**
     * Deletes the temporary bitmap file
     */
    private fun deleteBitmapFile() {
        val bitmapPath = getBitmapPath()
        bitmapPath?.let {
            val tempFile = File(it)
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    /**
     * Fetch the bitmap from a given path
     */
    private fun getBitmap(): Bitmap {
        val bitmapPath = getBitmapPath()
        return BitmapFactory.decodeFile(bitmapPath)
    }

    /**
     * Retrieves the path of the bitmap
     */
    private fun getBitmapPath() = arguments?.getString(BITMAP_PATH)

    companion object {
        const val BITMAP_PATH = "bitmap_path"
    }
}