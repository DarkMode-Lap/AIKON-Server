package team.darkmoderap.aikon.domain.exchange.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import team.darkmoderap.aikon.domain.exchange.service.ExchangeFileService

@RestController
@RequestMapping("/exchange")
class ExchangeController(
    private val exchangeFileService: ExchangeFileService,
) {
    @PostMapping("/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun uploadAvatar(
        @RequestPart("file") file: MultipartFile,
    ) {
        exchangeFileService.uploadAvatar(file)
    }

    @GetMapping("/report", produces = [MediaType.IMAGE_PNG_VALUE])
    fun downloadReport(): ByteArray = exchangeFileService.downloadReport()
}
