package com.laerodev.pdfdemoapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfLoader(private val context: Context, private val pdfFileName: String) {

    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pageCount: Int = 0

    init {
        openPdf()
    }

    private fun openPdf(){
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open(pdfFileName)
            val file = File(context.cacheDir, pdfFileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor!!)
            pageCount = pdfRenderer!!.pageCount
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun renderPage(pageIndex: Int): Bitmap? {
        if (pageIndex < 0 || pageIndex >= pageCount) {
            return null
        }
        currentPage?.close()
        currentPage = pdfRenderer?.openPage(pageIndex)
        val bitmap = Bitmap.createBitmap(currentPage!!.width, currentPage!!.height, Bitmap.Config.ARGB_8888)
        currentPage?.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    fun getPageCount(): Int {
        return pageCount
    }

    fun closePdf() {
        currentPage?.close()
        pdfRenderer?.close()
        fileDescriptor?.close()
    }





}