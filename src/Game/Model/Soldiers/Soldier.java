package Game.Model.Soldiers;

import Game.Model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Soldier extends Fightable implements Card {
    protected  Speed speed;
    protected final Target target;
    protected final boolean isAreaSplash;
    protected final int count;
    protected final int cost;
    protected FighterMode mode;
    protected Timer moveTimer;
    protected Timer fightTimer;
    protected long moveTime;
    protected long fightTime;


    public Soldier(Board board, int hp, int damage, double hitSpeed, double range, Location location, Speed speed, Target target,
                   boolean isAreaSplash, int count, int cost, Team team, FightableType type) {
        super(board, hp, damage, hitSpeed, range, location, team, type);
        this.speed = speed;
        this.target = target;
        this.isAreaSplash = isAreaSplash;
        this.count = count;
        this.cost = cost;
        fightTime = (long) hitSpeed * 1000;
        moveTime = getMoveTime();
    }

    @Override
    public void run() {
        live();
        die();
    }


    @Override
    public Fightable getNearestEnemy(double range) {
        double min = range;
        Fightable nearestEnemy = null;
        LinkedList<Fightable> enemy = (this.team.equals(Team.A)) ? board.getBFightables() : board.getAFightables();
        for (Fightable fightable : enemy) {
            if (this.location.getRegion().equals(fightable.getLocation().getRegion())) {
                if (this.location.getDistance(fightable.getLocation()) < min) {
                    if (isValidEnemy(fightable)) {
                        nearestEnemy = fightable;
                        min = this.location.getDistance(fightable.getLocation());
                    }
                }
            }
        }
        return nearestEnemy;
    }
    public void changeSpeed(){
        //todo : relationship between speed and numbers
    }


    public void fight(ArrayList<Fightable> targets) {
        mode = FighterMode.FIGHT;
        TimerTask fight = new TimerTask() {
            @Override
            public void run() {
                if(isAreaSplash)
                    updateTargetList(targets);
                if (targets.size() == 0)
                    return;
                for (Fightable fightable : targets) {
                    if (fightable.alive() == false)
                        targets.remove(fightable);
                    endamage(fightable);
                }
            }
        };
        fightTimer.schedule(fight,0,fightTime);
    }
    
    public void updateTargetList(ArrayList<Fightable> targets){
        LinkedList<Fightable> enemy = (this.team.equals(Team.A)) ? board.getBFightables() : board.getAFightables();
        for (Fightable fightable : enemy) {
            if (this.location.getRegion().equals(fightable.getLocation().getRegion())) {
                if (this.location.getDistance(fightable.getLocation()) <= range) {
                    targets.add(fightable);
                }
            }
        }
    }


    public void move(Location destination) {
        mode = FighterMode.MOVE;
        if (destination.getX() == location.getX()) {
            if (destination.getY() > location.getY())
                location.setY(location.getY() + 1);
            else
                location.setY(location.getY() - 1);
        } else {
            if (destination.getX() > location.getX())
                location.setX(location.getX() + 1);
            else
                location.setX(location.getX() - 1);
        }
    }

    public Location getNearestBridge() {
        double min = board.getLength();
        Location dest = null;
        if (location.getRegion().equals(Region.A)){
            for (Bridge bridge : board.getBridges()){
                if (bridge.getAHead().getDistance(location) < min){
                    min = bridge.getAHead().getDistance(location);
                    dest = bridge.getAHead();
                }
            }
        }else{
            for (Bridge bridge : board.getBridges()){
                if (bridge.getBHead().getDistance(location) < min){
                    min = bridge.getBHead().getDistance(location);
                    dest = bridge.getBHead();
                }
            }
        }
        return dest;
    }

    public void live() {
        ArrayList<Fightable> target = new ArrayList<>();
        target.add(getNearestEnemy(board.getSearchFightableRange()));
        TimerTask move = new TimerTask() {
            @Override
            public void run() {
                if (!alive)
                    return;
                if (target == null) {
                    Location dest = getNearestBridge();
                    move(dest);
                } else {
                    if (location.getDistance(target.get(0).getLocation()) <= range) {
                        fight(target);
                    } else {
                        Location dest = target.get(0).getLocation();
                        move(dest);
                    }
                }
            }
        };
        moveTimer.schedule(move,0,moveTime);
    }

    public long getMoveTime(){
        if (speed.equals(Speed.SLOW))
            return 3*1000;
        else if (speed.equals(Speed.MEDIUM))
            return 2*1000;
        else
            return 1*1000;
    }

    public void die(){
        if (team.equals(Team.A))
            board.getAFightables().remove(this);
        else
            board.getBFightables().remove(this);
    }

    public abstract boolean isValidEnemy(Fightable fightable);

    public Location getLocation() {
        return location;
    }
}
