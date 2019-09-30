package io.whalebone.publicapi.ejb.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class ResolverMetricsTimeBucketDTO {
    private ZonedDateTime timestamp;
    private Usage usage;
    private Latency latency;
    private String check;

    public static class Usage {
        private double cpu;
        private double memory;
        private double hdd;
        private double swap;

        public double getCpu() {
            return cpu;
        }

        public void setCpu(double cpu) {
            this.cpu = cpu;
        }

        public double getMemory() {
            return memory;
        }

        public void setMemory(double memory) {
            this.memory = memory;
        }

        public double getHdd() {
            return hdd;
        }

        public void setHdd(double hdd) {
            this.hdd = hdd;
        }

        public double getSwap() {
            return swap;
        }

        public void setSwap(double swap) {
            this.swap = swap;
        }
    }

    public static class Latency {
        @SerializedName("answers_1ms")
        private long answers1ms;
        @SerializedName("answers_10ms")
        private long answers10ms;
        @SerializedName("answers_50ms")
        private long answers50ms;
        @SerializedName("answers_100ms")
        private long answers100ms;
        @SerializedName("answers_250ms")
        private long answers250ms;
        @SerializedName("answers_500ms")
        private long answers500ms;
        @SerializedName("answers_1000ms")
        private long answers1000ms;
        @SerializedName("answers_1500ms")
        private long answers1500ms;

        public long getAnswers1ms() {
            return answers1ms;
        }

        public void setAnswers1ms(long answers1ms) {
            this.answers1ms = answers1ms;
        }

        public long getAnswers10ms() {
            return answers10ms;
        }

        public void setAnswers10ms(long answers10ms) {
            this.answers10ms = answers10ms;
        }

        public long getAnswers50ms() {
            return answers50ms;
        }

        public void setAnswers50ms(long answers50ms) {
            this.answers50ms = answers50ms;
        }

        public long getAnswers100ms() {
            return answers100ms;
        }

        public void setAnswers100ms(long answers100ms) {
            this.answers100ms = answers100ms;
        }

        public long getAnswers250ms() {
            return answers250ms;
        }

        public void setAnswers250ms(long answers250ms) {
            this.answers250ms = answers250ms;
        }

        public long getAnswers500ms() {
            return answers500ms;
        }

        public void setAnswers500ms(long answers500ms) {
            this.answers500ms = answers500ms;
        }

        public long getAnswers1000ms() {
            return answers1000ms;
        }

        public void setAnswers1000ms(long answers1000ms) {
            this.answers1000ms = answers1000ms;
        }

        public long getAnswers1500ms() {
            return answers1500ms;
        }

        public void setAnswers1500ms(long answers1500ms) {
            this.answers1500ms = answers1500ms;
        }
    }
}