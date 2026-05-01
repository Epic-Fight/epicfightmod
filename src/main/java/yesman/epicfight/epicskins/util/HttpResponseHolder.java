package yesman.epicfight.epicskins.util;

public record HttpResponseHolder(int statusCode, String body, Throwable exception) {
}
