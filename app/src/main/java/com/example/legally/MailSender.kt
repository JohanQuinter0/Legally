package com.example.legally

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object MailSender {

    fun generarOTP(): String {
        return (1000..9999).random().toString()
    }

    suspend fun enviarCorreo(correoDestino: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val otpGenerado = generarOTP()

                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.socketFactory.port", "465")
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.port", "465")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication("tuamigolegally@gmail.com", "iwkcqpujrovkkyxn")
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress("sebas.quintero06@gmail.com"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino))
                    subject = "Código de verificación"
                    setText("Hola,\n\nTu código de verificación es: $otpGenerado")
                }

                Transport.send(message)
                otpGenerado
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
