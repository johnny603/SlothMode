package io.slothmode.session;

import io.slothmode.model.ModelException;
import io.slothmode.model.SlothModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates the generation of a session summary using a local model.
 */
public class SessionSummaryAgent {
    private static final Logger logger = LoggerFactory.getLogger(SessionSummaryAgent.class);
    private final SlothModel model;
    private final EventIterator iterator;
    private final SummaryVisitor visitor;

    public SessionSummaryAgent(SlothModel model, EventIterator iterator, SummaryVisitor visitor) {
        this.model = model;
        this.iterator = iterator;
        this.visitor = visitor;
    }

    public String generateDigest() {
        try {
            iterator.First();
            while (!iterator.IsDone()) {
                visitor.visit(iterator.CurrentItem());
                iterator.Next();
            }
            return model.generate(visitor.prompt());
        } catch (ModelException e) {
            logger.error("Model failed to generate digest", e);
            return "Could not reach the local model. Steady as she goes.";
        }
    }
}
