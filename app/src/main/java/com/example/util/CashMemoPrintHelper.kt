package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.model.CashMemo
import com.example.data.model.ShopProfile
import com.example.data.model.takaInBengaliWords
import java.io.File
import java.io.FileOutputStream

enum class CashMemoStyle {
    ORANGE_AMBER, // ছবির হুবহু অরেঞ্জ-অ্যাম্বার এক্সিকিউটিভ ডিজাইন
    CYAN_BLUE     // সায়ান-ব্লু মোবাইল মেমো ডিজাইন
}

object CashMemoPrintHelper {

    fun printMemo(
        context: Context,
        memo: CashMemo,
        profile: ShopProfile?,
        style: CashMemoStyle = CashMemoStyle.ORANGE_AMBER
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "Yasin_Telecom_Memo_${memo.memoNumber}"

        printManager.print(jobName, object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder("Cash_Memo_${memo.memoNumber}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                pdfDocument = createPdfDocument(context, memo, profile, style)
                try {
                    val out = FileOutputStream(destination?.fileDescriptor)
                    pdfDocument?.writeTo(out)
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    pdfDocument?.close()
                }
            }
        }, null)
    }

    /**
     * Share directly to WhatsApp with pre-filled message and customer phone
     */
    fun shareToWhatsApp(context: Context, memo: CashMemo, profile: ShopProfile?) {
        val rawPhone = memo.customerMobile.trim().replace("+", "").replace("-", "").replace(" ", "")
        val formattedPhone = when {
            rawPhone.startsWith("01") -> "88$rawPhone"
            rawPhone.startsWith("1") -> "880$rawPhone"
            else -> rawPhone
        }
        val receiptText = getPlainTextReceipt(memo, profile)

        try {
            if (formattedPhone.isNotBlank()) {
                val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(receiptText)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setPackage("com.whatsapp")
                context.startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    `package` = "com.whatsapp"
                    putExtra(Intent.EXTRA_TEXT, receiptText)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            try {
                val webUrl = if (formattedPhone.isNotBlank()) {
                    "https://wa.me/$formattedPhone?text=${Uri.encode(receiptText)}"
                } else {
                    "https://api.whatsapp.com/send?text=${Uri.encode(receiptText)}"
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                sharePdf(context, memo, profile)
            }
        }
    }

    fun sharePdf(
        context: Context,
        memo: CashMemo,
        profile: ShopProfile?,
        style: CashMemoStyle = CashMemoStyle.ORANGE_AMBER
    ) {
        try {
            val pdf = createPdfDocument(context, memo, profile, style)
            val cacheDir = File(context.cacheDir, "memos")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, "Cash_Memo_${memo.memoNumber}.pdf")
            val fos = FileOutputStream(file)
            pdf.writeTo(fos)
            fos.close()
            pdf.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "ক্যাশ মেমো #${memo.memoNumber} - ${profile?.shopName ?: "ইয়াছিন টেলিকম"}")
                putExtra(Intent.EXTRA_TEXT, getPlainTextReceipt(memo, profile))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "ক্যাশ মেমো শেয়ার করুন"))
        } catch (e: Exception) {
            val textShare = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "ক্যাশ মেমো #${memo.memoNumber}")
                putExtra(Intent.EXTRA_TEXT, getPlainTextReceipt(memo, profile))
            }
            context.startActivity(Intent.createChooser(textShare, "ক্যাশ মেমো শেয়ার করুন"))
        }
    }

    fun getPlainTextReceipt(memo: CashMemo, profile: ShopProfile?): String {
        val sb = StringBuilder()
        sb.appendLine("🧾 *ক্যাশ মেমো / CASH MEMO* 🧾")
        sb.appendLine("🏢 *${profile?.shopName ?: "ইয়াছিন টেলিকম"}*")
        sb.appendLine("📱 স্মার্টফোন ও ডিজিটাল সেবা কেন্দ্র")
        sb.appendLine("👤 প্রোঃ মোহাম্মদ ইয়াছিন")
        sb.appendLine("📞 01766906974")
        sb.appendLine("📍 বুল্লা বাজার, লাখাই, হবিগঞ্জ")
        sb.appendLine("--------------------------------")
        sb.appendLine("মেমো নং: ${memo.memoNumber}")
        sb.appendLine("তারিখ: ${memo.date} (${memo.time})")
        sb.appendLine("গ্রাহকের নাম: ${memo.customerName.ifBlank { "নগদ ক্রেতা" }}")
        if (memo.customerMobile.isNotBlank()) sb.appendLine("মোবাইল: ${memo.customerMobile}")
        if (memo.customerAddress.isNotBlank()) sb.appendLine("ঠিকানা: ${memo.customerAddress}")
        sb.appendLine("--------------------------------")
        sb.appendLine("*পণ্যের বিবরণ:*")
        memo.items.forEachIndexed { i, item ->
            sb.appendLine("${i + 1}. ${item.description} - ৳${item.rate.toInt()} × ${item.quantity} = ৳${item.amount.toInt()}")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("💰 *মোট টাকা:* ৳${memo.subtotal.toInt()}")
        sb.appendLine("✅ *জমা:* ৳${memo.paidAmount.toInt()}")
        if (memo.dueAmount > 0) {
            sb.appendLine("⚠️ *বাকী:* ৳${memo.dueAmount.toInt()}")
        } else {
            sb.appendLine("✨ *পরিশোধিত*")
        }
        sb.appendLine("🗣️ *কথায়:* ${takaInBengaliWords(memo.grandTotal)}")
        sb.appendLine("--------------------------------")
        sb.appendLine("📌 বিক্রিত মাল ফেরত নেওয়া হয় না।")
        sb.appendLine("🙏 ধন্যবাদ, আবার আসবেন!")
        return sb.toString()
    }

    fun createPdfDocument(
        context: Context,
        memo: CashMemo,
        profile: ShopProfile?,
        style: CashMemoStyle = CashMemoStyle.ORANGE_AMBER
    ): PdfDocument {
        return if (style == CashMemoStyle.ORANGE_AMBER) {
            createOrangeAmberPdfDocument(context, memo, profile)
        } else {
            createCyanBluePdfDocument(context, memo, profile)
        }
    }

    /**
     * হুবহু ইউজার রেফারেন্স ছবির মতো: অরেঞ্জ-অ্যাম্বার এক্সিকিউটিভ ডিজাইন
     */
    private fun createOrangeAmberPdfDocument(context: Context, memo: CashMemo, profile: ShopProfile?): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. সাদা ব্যাকগ্রাউন্ড
        canvas.drawColor(Color.WHITE)

        // 2. টপ রাইট: ডায়নামিক অরেঞ্জ কার্ভড আর্চ ও সলিড ব্ল্যাক প্যানেল
        // সলিড ব্ল্যাক প্যানেল (ডান দিক)
        val blackPanelPath = Path().apply {
            moveTo(360f, 0f)
            cubicTo(370f, 60f, 340f, 120f, 380f, 135f)
            lineTo(595f, 135f)
            lineTo(595f, 0f)
            close()
        }
        val blackPanelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42) // #0F172A
        }
        canvas.drawPath(blackPanelPath, blackPanelPaint)

        // কার্ভড অরেঞ্জ গ্রেডিয়েন্ট অ্যাকসেন্ট স্ট্রাইপ
        val orangeWavePath = Path().apply {
            moveTo(330f, 0f)
            cubicTo(345f, 70f, 315f, 125f, 360f, 142f)
            lineTo(380f, 135f)
            cubicTo(340f, 120f, 370f, 60f, 360f, 0f)
            close()
        }
        val orangeWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(330f, 0f, 380f, 142f, Color.rgb(234, 88, 12), Color.rgb(245, 158, 11), Shader.TileMode.CLAMP)
        }
        canvas.drawPath(orangeWavePath, orangeWavePaint)

        // 3. টপ লেফট: ক্যাশ মেমো ক্যাপসুল ব্যাজ
        val capsulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42) // Dark Capsule
        }
        canvas.drawRoundRect(35f, 24f, 135f, 44f, 10f, 10f, capsulePaint)
        paint.color = Color.rgb(245, 158, 11) // Amber / Red text
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Cash Memo", 52f, 38f, paint)

        // COMPANY NAME / ইয়াছিন টেলিকম
        paint.color = Color.rgb(220, 38, 38) // Bold Orange-Red (#DC2626)
        paint.textSize = 25f
        paint.isFakeBoldText = true
        canvas.drawText(profile?.shopName ?: "ইয়াছিন টেলিকম", 35f, 76f, paint)

        // SLOGAN HERE
        paint.color = Color.rgb(234, 88, 12) // Orange (#EA580C)
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("এখানে যাবতীয় পাইকারী ও খুচরা মালামাল এবং ডিজিটাল সেবা সুলভ মূল্যে প্রদান করা হয়।", 35f, 96f, paint)

        // 4. রাইট ব্ল্যাক প্যানেলের কন্টাক্ট ইনফরমেশন (অরেঞ্জ আইকন সার্কেল সহ)
        val iconCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(234, 88, 12) // Orange Circle
        }
        var contactY = 32f
        val contacts = listOf(
            "📍" to "বুল্লা বাজার, লাখাই, হবিগঞ্জ",
            "👤" to "প্রোঃ মোহাম্মদ ইয়াছিন",
            "✉️" to "yasin.telecom@gmail.com",
            "📞" to "01766906974"
        )
        paint.textSize = 8.5f
        paint.isFakeBoldText = true

        contacts.forEach { (icon, text) ->
            canvas.drawCircle(405f, contactY - 3f, 7f, iconCirclePaint)
            paint.color = Color.WHITE
            canvas.drawText(icon, 400.5f, contactY, paint)

            paint.color = if (icon == "📞") Color.rgb(253, 224, 71) else Color.rgb(241, 245, 249)
            canvas.drawText(text, 420f, contactY, paint)
            contactY += 24f
        }

        // 5. গ্রাহক ও ইনভয়েস সেকশন (ছবিতে যেমন আছে)
        var y = 165f

        // No:
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("No: ${memo.memoNumber}", 35f, y, paint)

        // Date: ইনভার্টেড ব্ল্যাক বক্স ও ৩-ঘরের গ্রিড [DD] [MM] [YYYY]
        val dateBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(15, 23, 42) }
        canvas.drawRect(420f, y - 13f, 465f, y + 4f, dateBadge)
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Date:", 428f, y, paint)

        // ডেট সেল ৩টি
        val dateStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(465f, y - 13f, 498f, y + 4f, dateStroke)
        canvas.drawRect(498f, y - 13f, 531f, y + 4f, dateStroke)
        canvas.drawRect(531f, y - 13f, 565f, y + 4f, dateStroke)

        val dateParts = memo.date.split("-", "/")
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        if (dateParts.size >= 3) {
            canvas.drawText(dateParts[0], 474f, y, paint)
            canvas.drawText(dateParts[1], 507f, y, paint)
            canvas.drawText(dateParts[2], 537f, y, paint)
        } else {
            canvas.drawText(memo.date, 475f, y, paint)
        }

        y += 20f

        // Name: ইনভার্টেড ব্ল্যাক লেবেল ও রাউন্ডেড বক্স
        canvas.drawRoundRect(35f, y - 12f, 85f, y + 4f, 4f, 4f, dateBadge)
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Name:", 44f, y, paint)

        val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(90f, y - 13f, 565f, y + 5f, 6f, 6f, boxStroke)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        canvas.drawText(memo.customerName.ifBlank { "নগদ ক্রেতা" }, 100f, y, paint)

        y += 24f

        // Address: ইনভার্টেড ব্ল্যাক লেবেল ও রাউন্ডেড বক্স
        canvas.drawRoundRect(35f, y - 12f, 95f, y + 4f, 4f, 4f, dateBadge)
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Address:", 42f, y, paint)

        canvas.drawRoundRect(100f, y - 13f, 565f, y + 5f, 6f, 6f, boxStroke)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10.5f
        paint.isFakeBoldText = false
        val addressFull = "${memo.customerAddress.ifBlank { "বুল্লা বাজার" }}${if (memo.customerMobile.isNotBlank()) " | মোবাইল: ${memo.customerMobile}" else ""}"
        canvas.drawText(addressFull, 110f, y, paint)

        y += 22f

        // 6. প্রোডাক্ট টেবিল (ভিব্রান্ট অরেঞ্জ-রেড গ্রেডিয়েন্ট হেডার)
        val tableTop = y
        val tableHeadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(35f, y, 565f, y + 26f, Color.rgb(234, 88, 12), Color.rgb(153, 27, 27), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(35f, y, 565f, y + 26f, tableHeadPaint)

        paint.color = Color.WHITE
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        canvas.drawText("No:", 44f, y + 17f, paint)
        canvas.drawText("Description (পণ্যের বিবরণ)", 100f, y + 17f, paint)
        canvas.drawText("Quantity", 345f, y + 17f, paint)
        canvas.drawText("Rate", 430f, y + 17f, paint)
        canvas.drawText("Amount", 505f, y + 17f, paint)

        y += 26f

        // টেবিল গ্রিড: Quantity এবং Amount কলামে ওয়ার্ম পিচ/অ্যাম্বার ব্যাকগ্রাউন্ড
        val peachPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 247, 237) // #FFF7ED
        }
        val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        val rowsCount = maxOf(memo.items.size, 10)
        val rowHeight = 22f
        val tableBottom = y + (rowsCount * rowHeight)

        // ব্যাকগ্রাউন্ড পিচ কলাম
        canvas.drawRect(335f, y, 410f, tableBottom, peachPaint)
        canvas.drawRect(490f, y, 565f, tableBottom, peachPaint)

        // রো ডেটা
        for (i in 0 until rowsCount) {
            val currentY = y + (i * rowHeight)
            val item = memo.items.getOrNull(i)

            if (item != null) {
                paint.color = Color.rgb(100, 116, 139)
                paint.textSize = 10.5f
                paint.isFakeBoldText = false
                canvas.drawText("${i + 1}", 48f, currentY + 15f, paint)

                paint.color = Color.rgb(15, 23, 42)
                paint.isFakeBoldText = true
                canvas.drawText(item.description, 95f, currentY + 15f, paint)

                paint.isFakeBoldText = false
                canvas.drawText("${item.quantity}", 365f, currentY + 15f, paint)
                canvas.drawText("৳${item.rate.toInt()}", 430f, currentY + 15f, paint)

                paint.color = Color.rgb(234, 88, 12)
                paint.isFakeBoldText = true
                canvas.drawText("৳${item.amount.toInt()}", 505f, currentY + 15f, paint)
            }

            canvas.drawLine(35f, currentY + rowHeight, 565f, currentY + rowHeight, gridLinePaint)
        }

        // ভার্টিক্যাল গ্রিড লাইন
        canvas.drawLine(35f, tableTop, 35f, tableBottom, gridLinePaint)
        canvas.drawLine(80f, tableTop, 80f, tableBottom, gridLinePaint)
        canvas.drawLine(335f, tableTop, 335f, tableBottom, gridLinePaint)
        canvas.drawLine(410f, tableTop, 410f, tableBottom, gridLinePaint)
        canvas.drawLine(490f, tableTop, 490f, tableBottom, gridLinePaint)
        canvas.drawLine(565f, tableTop, 565f, tableBottom, gridLinePaint)

        y = tableBottom

        // 7. সামারি রোসমূহ (Total, Advance, Due) - ছবিতে যেমন ডান কোনায় আছে
        val summaryRows = listOf(
            "Total" to "৳${memo.subtotal.toInt()}",
            "Advance" to "৳${memo.paidAmount.toInt()}",
            "Due" to "৳${memo.dueAmount.toInt()}"
        )

        summaryRows.forEach { (label, value) ->
            canvas.drawRect(410f, y, 490f, y + rowHeight, peachPaint)
            paint.color = Color.rgb(234, 88, 12) // Orange Label
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText(label, 440f, y + 15f, paint)

            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(value, 505f, y + 15f, paint)

            canvas.drawLine(410f, y + rowHeight, 565f, y + rowHeight, gridLinePaint)
            canvas.drawLine(410f, y, 410f, y + rowHeight, gridLinePaint)
            canvas.drawLine(490f, y, 490f, y + rowHeight, gridLinePaint)
            canvas.drawLine(565f, y, 565f, y + rowHeight, gridLinePaint)

            y += rowHeight
        }

        y += 30f

        // 8. Taka (In word)....................................................
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val wordsText = "Taka (In word): ${takaInBengaliWords(memo.grandTotal)}..................................................................................."
        canvas.drawText(wordsText, 35f, y, paint)

        y += 65f

        // 9. সিগনেচার সেকশন
        val sigLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            strokeWidth = 1f
        }
        canvas.drawLine(40f, y, 180f, y, sigLinePaint)
        canvas.drawLine(415f, y, 555f, y, sigLinePaint)

        paint.textSize = 9.5f
        paint.color = Color.rgb(15, 23, 42)
        paint.isFakeBoldText = true
        canvas.drawText("Customer Signature", 55f, y + 14f, paint)
        canvas.drawText("Authorized Signature", 425f, y + 14f, paint)

        paint.textSize = 8.5f
        paint.color = Color.rgb(100, 116, 139)
        paint.isFakeBoldText = false
        canvas.drawText("(ক্রেতার স্বাক্ষর)", 75f, y + 26f, paint)
        canvas.drawText("(বিক্রেতার স্বাক্ষর)", 450f, y + 26f, paint)

        // 10. বটম ডেকোরেটিভ অরেঞ্জ কার্ভড স্ট্রাইপ (ছবির মতো)
        val bottomWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 825f, 595f, 842f, Color.rgb(234, 88, 12), Color.rgb(245, 158, 11), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 830f, 595f, 842f, bottomWavePaint)

        document.finishPage(page)
        return document
    }

    /**
     * সায়ান-ব্লু মোবাইল স্টাইল পিডিএফ
     */
    private fun createCyanBluePdfDocument(context: Context, memo: CashMemo, profile: ShopProfile?): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Top Cyan-Blue Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(2, 132, 199) // #0284C7 Royal/Sky Blue
        }
        canvas.drawRect(0f, 0f, 595f, 130f, headerPaint)

        // Dark accent bar
        val darkBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(7, 38, 59) // #07263B
        }
        canvas.drawRect(0f, 0f, 595f, 26f, darkBarPaint)

        // Brand Pills on top left in dark bar
        val brandPills = listOf("vivo", "oppo", "mi", "realme", "SAMSUNG", "1+")
        var pillX = 30f
        paint.textSize = 8.5f
        paint.isFakeBoldText = true

        val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 255, 255, 255)
        }
        brandPills.forEach { brand ->
            val w = paint.measureText(brand) + 12f
            canvas.drawRoundRect(pillX, 5f, pillX + w, 21f, 6f, 6f, pillBgPaint)
            paint.color = Color.rgb(7, 38, 59)
            canvas.drawText(brand, pillX + 6f, 16.5f, paint)
            pillX += w + 6f
        }

        // Amber/Yellow "ক্যাশ মেমো" badge on top right
        val yellowBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11) // #F59E0B Amber
        }
        canvas.drawRoundRect(465f, 5f, 565f, 22f, 8f, 8f, yellowBadgePaint)
        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("ক্যাশ মেমো", 485f, 17f, paint)

        // Shop Title: “ইয়াছিন টেলিকম”
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(profile?.shopName ?: "ইয়াছিন টেলিকম", 40f, 60f, paint)

        // Subtitle: স্মার্টফোন ও ডিজিটাল সেবা কেন্দ্র
        paint.color = Color.rgb(224, 242, 254) // #E0F2FE
        paint.textSize = 11.5f
        paint.isFakeBoldText = false
        canvas.drawText("স্মার্টফোন ও ডিজিটাল সেবা কেন্দ্র", 40f, 78f, paint)

        // Owner & Phone on the right
        paint.color = Color.rgb(254, 226, 226) // Soft Red
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("➤ প্রোঃ মোহাম্মদ ইয়াছিন ◄", 360f, 60f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11.5f
        canvas.drawText("📞 01766906974", 360f, 78f, paint)

        // Red Capsule Tagline Bar
        val taglineBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 38, 38) // Red capsule
        }
        canvas.drawRoundRect(30f, 88f, 565f, 106f, 9f, 9f, taglineBgPaint)
        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        canvas.drawText("এখানে যাবতীয় পাইকারী ও খুচরা মালামাল এবং ডিজিটাল সেবা সুলভ মূল্যে প্রদান করা হয়।", 60f, 100.5f, paint)

        // Sky-Blue Address Bar
        val addrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(3, 105, 161)
        }
        canvas.drawRect(0f, 110f, 595f, 130f, addrBgPaint)
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("📍 বুল্লা বাজার, লাখাই, হবিগঞ্জ", 210f, 124f, paint)

        var y = 152f

        // 2. Customer Information (Badge Style)
        val redBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(220, 38, 38) }
        canvas.drawRoundRect(30f, y - 13f, 80f, y + 4f, 6f, 6f, redBadge)
        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("নামঃ", 42f, y, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText(memo.customerName.ifBlank { "নগদ ক্রেতা" }, 90f, y, paint)

        // Green badge (Date)
        val greenBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(22, 163, 74) }
        canvas.drawRoundRect(400f, y - 13f, 450f, y + 4f, 6f, 6f, greenBadge)
        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("তারিখঃ", 408f, y, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10.5f
        paint.isFakeBoldText = false
        canvas.drawText(memo.date, 460f, y, paint)

        y += 24f

        // Row 2: Serial / Memo No (Purple badge) and Mobile (Blue badge)
        val purpleBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(124, 58, 237) }
        canvas.drawRoundRect(30f, y - 13f, 95f, y + 4f, 6f, 6f, purpleBadge)
        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        canvas.drawText("ক্রমিক নংঃ", 38f, y, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        canvas.drawText(memo.memoNumber, 105f, y, paint)

        // Blue badge (Mobile)
        val blueBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(2, 132, 199) }
        canvas.drawRoundRect(400f, y - 13f, 455f, y + 4f, 6f, 6f, blueBadge)
        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        canvas.drawText("মোবাইলঃ", 406f, y, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10.5f
        paint.isFakeBoldText = false
        canvas.drawText(memo.customerMobile.ifBlank { "N/A" }, 465f, y, paint)

        y += 24f

        // Row 3: Address (Green badge)
        canvas.drawRoundRect(30f, y - 13f, 85f, y + 4f, 6f, 6f, greenBadge)
        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("ঠিকানাঃ", 40f, y, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10.5f
        paint.isFakeBoldText = false
        canvas.drawText(memo.customerAddress.ifBlank { "বুল্লা বাজার" }, 95f, y, paint)

        y += 18f

        // 3. Product Table (Cyan-Blue Rounded Table Header)
        val tableHeadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(2, 132, 199)
        }
        canvas.drawRoundRect(30f, y, 565f, y + 26f, 8f, 8f, tableHeadPaint)

        paint.color = Color.WHITE
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        canvas.drawText("ক্র.নং", 42f, y + 17f, paint)
        canvas.drawText("বিবরণ (Description)", 100f, y + 17f, paint)
        canvas.drawText("পরিমাণ", 340f, y + 17f, paint)
        canvas.drawText("দর (৳)", 420f, y + 17f, paint)
        canvas.drawText("টাকা (৳)", 500f, y + 17f, paint)

        y += 26f

        val rowLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        paint.textSize = 10.5f
        paint.isFakeBoldText = false

        memo.items.forEachIndexed { idx, item ->
            y += 22f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText("${idx + 1}", 46f, y, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.isFakeBoldText = true
            canvas.drawText(item.description, 100f, y, paint)

            paint.isFakeBoldText = false
            canvas.drawText("${item.quantity}", 355f, y, paint)
            canvas.drawText("৳${item.rate.toInt()}", 425f, y, paint)

            paint.color = Color.rgb(2, 132, 199)
            paint.isFakeBoldText = true
            canvas.drawText("৳${item.amount.toInt()}", 505f, y, paint)

            canvas.drawLine(30f, y + 6f, 565f, y + 6f, rowLinePaint)
        }

        y += 24f

        // 4. Bottom Calculations & In Words Section
        val cyanBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(6, 182, 212) }
        canvas.drawRoundRect(30f, y - 12f, 85f, y + 4f, 6f, 6f, cyanBadge)
        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("কথায়ঃ", 40f, y, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText(takaInBengaliWords(memo.grandTotal), 95f, y, paint)

        // Right Column: Badged Totals
        canvas.drawRoundRect(380f, y - 13f, 450f, y + 5f, 6f, 6f, greenBadge)
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("মোট টাকাঃ", 390f, y, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("৳${memo.subtotal.toInt()}", 475f, y, paint)

        y += 24f

        val noteBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 245, 249)
        }
        canvas.drawRoundRect(30f, y - 8f, 340f, y + 45f, 8f, 8f, noteBoxPaint)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("📌 বিক্রিত মাল ফেরত নেওয়া হয় না।", 40f, y + 10f, paint)
        paint.color = Color.rgb(22, 163, 74)
        paint.isFakeBoldText = true
        canvas.drawText("🙏 ধন্যবাদ, আবার আসবেন!", 40f, y + 28f, paint)

        canvas.drawRoundRect(380f, y - 13f, 435f, y + 5f, 6f, 6f, redBadge)
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("জমাঃ", 395f, y, paint)

        paint.color = Color.rgb(22, 163, 74)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("৳${memo.paidAmount.toInt()}", 475f, y, paint)

        y += 24f

        canvas.drawRoundRect(380f, y - 13f, 435f, y + 5f, 6f, 6f, blueBadge)
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("বাকীঃ", 395f, y, paint)

        paint.color = if (memo.dueAmount > 0) Color.rgb(220, 38, 38) else Color.rgb(100, 116, 139)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("৳${memo.dueAmount.toInt()}", 475f, y, paint)

        y += 60f

        // 5. Signatures Footer
        canvas.drawLine(50f, y, 190f, y, rowLinePaint)
        canvas.drawLine(390f, y, 545f, y, rowLinePaint)

        paint.textSize = 10f
        paint.color = Color.rgb(100, 116, 139)
        paint.isFakeBoldText = false
        canvas.drawText("ক্রেতার স্বাক্ষর", 90f, y + 16f, paint)

        paint.color = Color.rgb(2, 132, 199)
        paint.isFakeBoldText = true
        canvas.drawText("বিক্রেতার স্বাক্ষর (ইয়াছিন টেলিকম)", 395f, y + 16f, paint)

        document.finishPage(page)
        return document
    }
}
