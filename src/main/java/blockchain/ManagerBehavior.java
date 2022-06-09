package blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import model.Block;
import model.HashResult;

import java.io.Serializable;
import java.util.Objects;

public class ManagerBehavior  extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable{}

    public static class HashResultCommand implements Command{
        private static final long serialversionUID = 1L;

        public HashResultCommand(HashResult hashResult) {
            this.hashResult = hashResult;
        }

        public HashResult getHashResult() {
            return hashResult;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HashResultCommand that = (HashResultCommand) o;
            return Objects.equals(hashResult, that.hashResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hashResult);
        }

        private HashResult hashResult;

    }
    public static class MineBlockCommand implements Command{
        private static final long serialversionUID = 1L;
        private final int difficulty;
        private Block block;

        public MineBlockCommand(Block block, ActorRef<HashResult> sender, int difficulty) {
            this.block = block;
            this.sender = sender;
            this.difficulty = difficulty;
        }

        private ActorRef<HashResult> sender;


        public Block getBlock() {
            return block;
        }

        public ActorRef<HashResult> getSender() {
            return sender;
        }

        public int getDifficulty() {
            return difficulty;
        }
    }
    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create(){
      return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
    return newReceiveBuilder()
            .onMessage(MineBlockCommand.class, message->{
                this.sender = message.getSender();
                this.block = message.getBlock();
                this.difficulty = message.getDifficulty();
                for(int i=0;i<10;i++){
                    startNextWorker();
                }
                return Behaviors.same();

            })
            .build();
    }

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficulty;
    private int currentNonce=0;

    private void startNextWorker(){
        ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.create(), "worker" +currentNonce);
        worker.tell(new WorkerBehavior.Command(block, currentNonce*1000, difficulty, getContext().getSelf()));
        currentNonce++;
    }
}
