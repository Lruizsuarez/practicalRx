package org.dogepool.practicalrx.services;

import org.dogepool.practicalrx.error.DogePoolException;
import org.dogepool.practicalrx.error.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.util.Map;

/**
 * A facade service to get DOGE to USD and DOGE to other currencies exchange rates.
 */
@Service
public class ExchangeRateService {

    @Value("${doge.api.baseUrl}")
    private String dogeUrl;

    @Value("${exchange.free.api.baseUrl}")
    private String exchangeUrl;

    @Value("${exchange.nonfree.api.baseUrl}")
    private String exchangeFreeUrl;

    @Autowired
    private RestTemplate restTemplate;

    public Observable<Double> dogeToCurrencyExchangeRate(String targetCurrencyCode) {
        return dogeToDollar()
                .flatMap(x -> dollarToCurrency(targetCurrencyCode)
                        .onErrorResumeNext(t -> t instanceof DogePoolException?
                                dollarToCurrencyNonFree(targetCurrencyCode):
                                Observable.error(t))
                        .map(currency -> x * currency));
    }

    private Observable<Double> dogeToDollar() {
        return Observable.<Double>create(subscriber -> {
            try {
                subscriber.onNext(restTemplate.getForObject(dogeUrl, Double.class));
                subscriber.onCompleted();
            } catch (RestClientException e) {
                throw new DogePoolException("Unable to reach doge rate service at " + dogeUrl,
                        Error.UNREACHABLE_SERVICE, HttpStatus.REQUEST_TIMEOUT);
            }
        }).doOnNext(response -> System.out.println("dogeToDollar response : " + response));
    }

    private Observable<Double> dollarToCurrency(String currencyCode) {
        return Observable.<Double>create(subscriber -> {
            try {
                Map result = restTemplate.getForObject(exchangeUrl + "/{from}/{to}", Map.class,
                        "USD", currencyCode);
                Double rate = (Double) result.get("exchangeRate");
                if (rate == null)
                    rate = (Double) result.get("rate");

                if (rate == null) {
                    throw new DogePoolException("Malformed exchange rate", Error.BAD_CURRENCY, HttpStatus.UNPROCESSABLE_ENTITY);
                }
                subscriber.onNext(rate);
                subscriber.onCompleted();
            } catch (HttpStatusCodeException e) {
                throw new DogePoolException("Error processing currency in free API : " + e.getResponseBodyAsString(),
                        Error.BAD_CURRENCY, e.getStatusCode());
            } catch (RestClientException e) {
                throw new DogePoolException("Unable to reach currency exchange service at " + exchangeUrl,
                        Error.UNREACHABLE_SERVICE, HttpStatus.REQUEST_TIMEOUT);
            }
        }).doOnNext(response -> System.out.println("dollarToCurrency response :" + response));
    }


    private Observable<Double> dollarToCurrencyNonFree(String currencyCode) {
        return Observable.<Double>create(subscriber -> {
            try {
                Map result = restTemplate.getForObject(exchangeFreeUrl + "/{from}/{to}", Map.class,
                        "USD", currencyCode);
                Double rate = (Double) result.get("exchangeRate");
                if (rate == null)
                    rate = (Double) result.get("rate");

                if (rate == null) {
                    throw new DogePoolException("Malformed exchange rate", Error.BAD_CURRENCY, HttpStatus.UNPROCESSABLE_ENTITY);
                }
                subscriber.onNext(rate);
                subscriber.onCompleted();
            } catch (HttpStatusCodeException e) {
                throw new DogePoolException("Error processing currency in free API : " + e.getResponseBodyAsString(),
                        Error.BAD_CURRENCY, e.getStatusCode());
            } catch (RestClientException e) {
                throw new DogePoolException("Unable to reach currency exchange service at " + exchangeUrl,
                        Error.UNREACHABLE_SERVICE, HttpStatus.REQUEST_TIMEOUT);
            }
        }).doOnNext(response -> System.out.println("dollarToCurrency response :" + response));
    }
}
