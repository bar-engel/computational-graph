package graph;

import java.util.function.BinaryOperator;

/**
 * An agent that waits for values on two input topics, applies a binary operation,
 * and publishes the result to an output topic.
 */
public class BinOpAgent implements Agent {

    private final String name;
    private final String topic1;
    private final String topic2;
    private final String outputTopic;
    private final BinaryOperator<Double> operation;

    private double input1 = Double.NaN;
    private double input2 = Double.NaN;

    public BinOpAgent(String name, String topic1, String topic2,
                      String outputTopic, BinaryOperator<Double> operation) {
        this.name = name;
        this.topic1 = topic1;
        this.topic2 = topic2;
        this.outputTopic = outputTopic;
        this.operation = operation;

        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(topic1).subscribe(this);
        tm.getTopic(topic2).subscribe(this);
        tm.getTopic(outputTopic).addPublisher(this);
    }

    @Override
    public void callback(String topic, Message msg) {
        if (topic.equals(topic1)) input1 = msg.asDouble;
        if (topic.equals(topic2)) input2 = msg.asDouble;

        if (!Double.isNaN(input1) && !Double.isNaN(input2)) {
            double result = operation.apply(input1, input2);
            TopicManagerSingleton.get().getTopic(outputTopic).publish(new Message(result));
            input1 = Double.NaN;
            input2 = Double.NaN;
        }
    }

    @Override
    public void reset() {
        input1 = Double.NaN;
        input2 = Double.NaN;
    }

    @Override
    public String getName() { return name; }

    @Override
    public void close() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(topic1).unsubscribe(this);
        tm.getTopic(topic2).unsubscribe(this);
    }
}
