package com.musclefit.app.data.db;

import java.util.ArrayList;
import java.util.List;

public final class SeedData {
    private SeedData() {
    }

    public static void populate(AppDatabase db) {
        List<ExerciseEntity> exercises = createExercises();
        db.exerciseDao().insertAll(exercises);
        for (ExerciseEntity exercise : exercises) {
            db.exerciseDao().updateDescription(exercise.id, exercise.description);
        }
        db.intensityDao().insertAll(createIntensityNotes());
    }

    private static List<ExerciseEntity> createExercises() {
        List<ExerciseEntity> list = new ArrayList<>();

        list.add(exercise(
                1,
                "杠铃卧推",
                "BARBELL",
                "COMPOUND",
                "1. 仰卧在平板凳上，双脚踩稳地面，肩胛后缩下沉并保持胸椎稳定。\n" +
                        "2. 杠铃从肩关节正上方缓慢下放至胸线附近，前臂尽量保持垂直地面。\n" +
                        "3. 呼气将杠铃向上推回起始位，手肘接近伸直但不过度锁死。",
                "正握",
                "杠铃训练",
                "下放不触胸反弹，腰背保持自然弓。",
                "胸大肌",
                32,
                18,
                4
        ));

        list.add(exercise(
                2,
                "哑铃侧平举",
                "DUMBBELL",
                "ISOLATION",
                "1. 双脚与肩同宽站立，核心收紧，双手持哑铃自然垂于身体两侧。\n" +
                        "2. 手肘微屈向两侧抬臂，抬至与肩同高时短暂停顿。\n" +
                        "3. 吸气控制下放至起始位，避免借惯性快速下落。",
                "中立握",
                "哑铃训练",
                "避免借力摆动，肩峰不耸肩。",
                "三角肌中束",
                26,
                20,
                3
        ));

        list.add(exercise(
                3,
                "绳索下压",
                "CABLE",
                "ISOLATION",
                "1. 面向下拉器站立，双手握住把手，手肘夹紧躯干两侧。\n" +
                        "2. 呼气将前臂向下压至手肘接近完全伸展，顶峰收紧肱三头肌。\n" +
                        "3. 吸气缓慢回到起始角度，全程保持上臂位置稳定。",
                "反握/正握",
                "绳索训练",
                "肘部不要前后摆动，避免耸肩代偿。",
                "肱三头肌",
                17,
                11,
                2
        ));

        list.add(exercise(
                4,
                "器械腿举",
                "MACHINE",
                "COMPOUND",
                "1. 坐入腿举机，腰背贴紧靠垫，双脚与肩同宽放在踏板中部。\n" +
                        "2. 缓慢屈膝下放重量至膝关节约90度，保持膝盖与脚尖同向。\n" +
                        "3. 呼气用脚跟发力推起踏板，回到起始位但不要锁死膝关节。",
                "",
                "器械训练",
                "膝盖方向与脚尖一致，不要锁死膝关节。",
                "股四头肌",
                41,
                24,
                5
        ));

        list.add(exercise(
                5,
                "俯卧撑",
                "BODYWEIGHT",
                "COMPOUND",
                "1. 双手略宽于肩撑地，肩-髋-踝保持一条直线，核心与臀部收紧。\n" +
                        "2. 屈肘下放身体至胸部接近地面，肘部约与躯干呈45度夹角。\n" +
                        "3. 呼气推地回到起始位，过程中避免塌腰或耸肩。",
                "",
                "自重训练",
                "避免塌腰和耸肩，保持颈部中立。",
                "胸大肌",
                39,
                28,
                3
        ));

        list.add(exercise(
                6,
                "引体向上",
                "BODYWEIGHT",
                "COMPOUND",
                "1. 双手宽于肩正握单杠，肩胛下沉，身体自然垂悬并收紧核心。\n" +
                        "2. 呼气向上拉起身体，目标是下巴越过单杠且躯干保持稳定。\n" +
                        "3. 吸气缓慢下放至手臂接近伸直，避免借摆动完成动作。",
                "正握",
                "自重训练",
                "避免耸肩借力和摆腿，下降阶段保持控制。",
                "背阔肌",
                34,
                19,
                4
        ));

        list.add(exercise(
                7,
                "平板支撑",
                "BODYWEIGHT",
                "ISOLATION",
                "1. 前臂与脚尖支撑地面，手肘位于肩关节正下方。\n" +
                        "2. 收紧腹部和臀部，使身体从头到脚形成稳定直线。\n" +
                        "3. 均匀呼吸并维持姿势到目标时长，全程避免塌腰或抬臀。",
                "",
                "自重训练",
                "避免塌腰和抬臀，保持均匀呼吸。",
                "腹横肌",
                23,
                14,
                2
        ));

        list.add(exercise(
                8,
                "自重深蹲",
                "BODYWEIGHT",
                "COMPOUND",
                "1. 双脚与肩同宽站立，脚尖微外展，核心收紧保持躯干稳定。\n" +
                        "2. 屈髋屈膝向下蹲，直到大腿接近平行地面，膝盖方向跟随脚尖。\n" +
                        "3. 呼气通过脚跟发力站起，回到站立位并保持膝关节稳定。",
                "",
                "自重训练",
                "膝盖与脚尖方向一致，起身时不要内扣。",
                "股四头肌",
                29,
                17,
                3
        ));

        list.add(exercise(
                9,
                "反向撑体臂屈伸",
                "BODYWEIGHT",
                "COMPOUND",
                "1. 双手放在凳边支撑身体，双脚向前伸出，肩胛保持下沉。\n" +
                        "2. 屈肘下放身体至上臂接近平行地面，肘部尽量朝后。\n" +
                        "3. 呼气用肱三头肌发力推起，回到起始位后继续下一次。",
                "中立握",
                "自重训练",
                "肩部保持下沉，避免下放过深导致前肩不适。",
                "肱三头肌",
                22,
                13,
                3
        ));

        list.add(exercise(
                10,
                "俯身倒V撑",
                "BODYWEIGHT",
                "COMPOUND",
                "1. 进入倒V姿势，双手与肩同宽撑地，臀部抬高形成稳定三角。\n" +
                        "2. 屈肘下放头部至双手之间，注意肩颈放松、核心持续收紧。\n" +
                        "3. 呼气将身体推回倒V起始位，保持动作节奏平稳。",
                "",
                "自重训练",
                "手肘微向后，核心收紧，避免腰椎代偿。",
                "三角肌前束",
                20,
                11,
                4
        ));

        list.add(exercise(
                11,
                "仰卧卷腹",
                "BODYWEIGHT",
                "ISOLATION",
                "1. 仰卧屈膝，双脚平放地面，双手轻放耳侧或胸前。\n" +
                        "2. 呼气收紧腹部，将肩胛骨卷离地面而非整段坐起。\n" +
                        "3. 顶峰停顿1秒后吸气缓慢回落，保持腹部持续张力。",
                "",
                "自重训练",
                "避免用手拉颈，发力集中在腹部。",
                "腹直肌",
                27,
                16,
                3
        ));

        list.add(exercise(
                12,
                "臀桥",
                "BODYWEIGHT",
                "ISOLATION",
                "1. 仰卧屈膝，脚跟靠近臀部，双手放于身体两侧稳定躯干。\n" +
                        "2. 呼气用脚跟发力抬髋，直到膝-髋-肩接近一条直线。\n" +
                        "3. 顶峰收紧臀部1秒后缓慢下放，保持骨盆控制。",
                "",
                "自重训练",
                "顶峰时收紧臀部，避免腰部过度后仰。",
                "臀大肌",
                24,
                12,
                3
        ));

        list.add(exercise(
                13,
                "超人挺身",
                "BODYWEIGHT",
                "ISOLATION",
                "1. 俯卧于垫上，双臂向前伸直，双腿自然伸直并收紧核心。\n" +
                        "2. 同时抬起双臂、胸部与双腿离地，感受背部后链发力。\n" +
                        "3. 顶峰短暂停顿后缓慢还原，避免快速甩动身体。",
                "",
                "自重训练",
                "动作幅度以可控为主，避免颈部后仰。",
                "背部竖脊肌",
                19,
                9,
                3
        ));

        list.add(exercise(
                14,
                "弓步蹲",
                "BODYWEIGHT",
                "COMPOUND",
                "1. 前后分腿站立，前脚全脚掌着地，后脚脚尖点地保持平衡。\n" +
                        "2. 屈膝下放至后膝接近地面，前膝与前脚尖方向一致。\n" +
                        "3. 前脚跟发力起身回到起始位，再换边重复。",
                "",
                "自重训练",
                "前脚跟稳定发力，膝盖不内扣。",
                "股四头肌",
                25,
                15,
                4
        ));

        list.add(exercise(
                15,
                "站姿提踵",
                "BODYWEIGHT",
                "ISOLATION",
                "1. 双脚与髋同宽站立，可扶墙或扶杆保持身体稳定。\n" +
                        "2. 呼气将脚跟抬至最高点，感受小腿明显收缩。\n" +
                        "3. 顶峰停顿后缓慢下放到起始位，全程控制节奏。",
                "",
                "自重训练",
                "全程控制节奏，避免借惯性弹动。",
                "腓肠肌",
                18,
                8,
                3
        ));

        list.add(exercise(
                16,
                "钻石俯卧撑",
                "BODYWEIGHT",
                "COMPOUND",
                "1. 双手拇指与食指靠近形成菱形，置于胸前下方撑地。\n" +
                        "2. 屈肘下放身体至胸部接近手背位置，保持核心稳定。\n" +
                        "3. 呼气推地回起始位，重点感受肱三头肌与胸部发力。",
                "",
                "自重训练",
                "保持核心稳定，避免肩肘不适时继续加量。",
                "肱三头肌",
                21,
                10,
                4
        ));

        list.add(exercise(
                17,
                "登山跑",
                "BODYWEIGHT",
                "COMPOUND",
                "1. 从高位俯撑开始，双手位于肩下，身体保持平板姿态。\n" +
                        "2. 交替将膝盖向胸部快速驱动，骨盆尽量保持稳定不晃动。\n" +
                        "3. 按目标时间或次数完成，节奏均匀并保持呼吸。",
                "",
                "自重训练",
                "避免臀部上下大幅摆动，保持呼吸节奏。",
                "腹直肌",
                28,
                14,
                3
        ));

        list.add(exercise(
                18,
                "单腿臀桥",
                "BODYWEIGHT",
                "ISOLATION",
                "1. 仰卧屈膝，一只脚踩地，另一腿伸直并抬离地面。\n" +
                        "2. 踩地脚跟发力抬髋至躯干与大腿成一直线，顶部收紧臀腿。\n" +
                        "3. 缓慢下放至接近地面后再次抬起，完成后换边训练。",
                "",
                "自重训练",
                "骨盆保持稳定，避免一侧塌陷或借力摆动。",
                "腘绳肌",
                16,
                7,
                3
        ));

        return list;
    }

    private static List<ExerciseMuscleIntensityEntity> createIntensityNotes() {
        List<ExerciseMuscleIntensityEntity> list = new ArrayList<>();
        list.add(note(1, "胸大肌", 4, "主"));
        list.add(note(1, "肱三头肌", 2, "辅"));
        list.add(note(1, "三角肌前束", 3, "辅"));

        list.add(note(2, "三角肌中束", 3, "主"));

        list.add(note(3, "肱三头肌", 2, "主"));

        list.add(note(4, "股四头肌", 5, "主"));
        list.add(note(4, "臀大肌", 4, "辅"));
        list.add(note(4, "腘绳肌", 3, "辅"));

        list.add(note(5, "胸大肌", 3, "主"));
        list.add(note(5, "三角肌前束", 2, "辅"));
        list.add(note(5, "肱三头肌", 2, "辅"));

        list.add(note(6, "背阔肌", 4, "主"));
        list.add(note(6, "肱二头肌", 3, "辅"));
        list.add(note(6, "前臂屈肌群", 2, "辅"));

        list.add(note(7, "腹横肌", 2, "主"));
        list.add(note(7, "腹直肌", 2, "辅"));

        list.add(note(8, "股四头肌", 3, "主"));
        list.add(note(8, "臀大肌", 2, "辅"));
        list.add(note(8, "腘绳肌", 2, "辅"));

        list.add(note(9, "肱三头肌", 3, "主"));
        list.add(note(9, "胸大肌", 2, "辅"));
        list.add(note(9, "三角肌前束", 2, "辅"));

        list.add(note(10, "三角肌前束", 4, "主"));
        list.add(note(10, "肱三头肌", 2, "辅"));
        list.add(note(10, "腹直肌", 2, "辅"));

        list.add(note(11, "腹直肌", 3, "主"));
        list.add(note(11, "腹斜肌", 2, "辅"));

        list.add(note(12, "臀大肌", 3, "主"));
        list.add(note(12, "腘绳肌", 2, "辅"));

        list.add(note(13, "背部竖脊肌", 3, "主"));
        list.add(note(13, "背阔肌", 2, "辅"));
        list.add(note(13, "臀大肌", 2, "辅"));

        list.add(note(14, "股四头肌", 4, "主"));
        list.add(note(14, "臀大肌", 3, "辅"));
        list.add(note(14, "腘绳肌", 2, "辅"));

        list.add(note(15, "腓肠肌", 3, "主"));
        list.add(note(15, "比目鱼肌", 2, "辅"));

        list.add(note(16, "肱三头肌", 4, "主"));
        list.add(note(16, "胸大肌", 3, "辅"));
        list.add(note(16, "三角肌前束", 2, "辅"));

        list.add(note(17, "腹直肌", 3, "主"));
        list.add(note(17, "髋屈肌", 2, "辅"));

        list.add(note(18, "腘绳肌", 3, "主"));
        list.add(note(18, "臀大肌", 2, "辅"));

        return list;
    }

    private static ExerciseEntity exercise(
            long id,
            String name,
            String trainingCategory,
            String movementType,
            String description,
            String gripType,
            String categoryHint,
            String caution,
            String primaryMuscle,
            int likeCount,
            int favoriteCount,
            int maxIntensity
    ) {
        ExerciseEntity e = new ExerciseEntity();
        e.id = id;
        e.name = name;
        e.trainingCategory = trainingCategory;
        e.movementType = movementType;
        e.description = description;
        e.gripType = gripType;
        e.categoryHint = categoryHint;
        e.cautionNotes = caution;
        e.primaryMuscle = primaryMuscle;
        e.likeCount = likeCount;
        e.favoriteCount = favoriteCount;
        e.maxIntensityLevel = maxIntensity;
        return e;
    }

    private static ExerciseMuscleIntensityEntity note(long exerciseId, String muscleName, int level, String role) {
        ExerciseMuscleIntensityEntity note = new ExerciseMuscleIntensityEntity();
        note.exerciseId = exerciseId;
        note.muscleName = muscleName;
        note.intensityLevel = level;
        note.role = role;
        return note;
    }
}
