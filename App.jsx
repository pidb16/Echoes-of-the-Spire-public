import { useState, useEffect, useRef, useCallback } from "react";

// ═══════════════════════════════════════════════════════════════
// SAVE / LOAD
// ═══════════════════════════════════════════════════════════════
const SAVE_KEY = "eots_save_v4";
function loadSave() {
  try {
    const raw = localStorage.getItem(SAVE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch { return null; }
}
function writeSave(state) {
  try { localStorage.setItem(SAVE_KEY, JSON.stringify(state)); } catch {}
}

// ═══════════════════════════════════════════════════════════════
// DATA
// ═══════════════════════════════════════════════════════════════
const CLASSES = {
  wanderer:   { name:"Wanderer",    icon:"⚔️", hp:140,atk:14,def:5, arcane:5, intel:3, faith:3, crit:.05, desc:"Balanced. Any weapon or build." },
  arcanist:   { name:"Arcanist",    icon:"🔮", hp:90, atk:10,def:2, arcane:15,intel:10,faith:2, crit:.06, desc:"Poison & Frost master." },
  pyromancer: { name:"Pyromancer",  icon:"🔥", hp:110,atk:12,def:3, arcane:3, intel:6, faith:14,crit:.07, desc:"Fire & Holy — Consecrated healer." },
  ironclad:   { name:"Ironclad",    icon:"🛡️", hp:220,atk:10,def:15,arcane:2, intel:2, faith:5, crit:.03, desc:"Tank. Life-steal synergy." },
  paladin:    { name:"Paladin",     icon:"⚜️", hp:180,atk:11,def:12,arcane:1, intel:2, faith:16,crit:.04, desc:"Holy Shield absorbs damage every 10s." },
  shadowblade:{ name:"Shadowblade", icon:"🌑", hp:80, atk:16,def:2, arcane:4, intel:3, faith:1, crit:.15, desc:"Every 3 hits = guaranteed crit (Stealth)." },
  spellblade: { name:"Spellblade",  icon:"✨", hp:105,atk:11,def:4, arcane:8, intel:12,faith:4, crit:.08, desc:"Hybrid: scales off both ATK and Intelligence." },
};

const WEAPONS = {
  fists:     { name:"Fists",      icon:"👊",atkMul:1.00,spdMs:1200,critAdd:.00,desc:"Raw power.",                   defPen:0,  twin:false,stun:0,    focus:false },
  dagger:    { name:"Daggers",    icon:"🗡️",atkMul:0.75,spdMs:680, critAdd:.12,desc:"Very fast, high crit.",        defPen:0,  twin:false,stun:0,    focus:false },
  sword:     { name:"Longsword",  icon:"⚔️",atkMul:1.00,spdMs:1050,critAdd:.04,desc:"Balanced, reliable.",          defPen:0,  twin:false,stun:0,    focus:false },
  greatsword:{ name:"Greatsword", icon:"🪓",atkMul:1.60,spdMs:1750,critAdd:.09,desc:"Slow but devastating.",        defPen:0,  twin:false,stun:0,    focus:false },
  spellstaff:{ name:"Spell Staff",icon:"🪄",atkMul:0.90,spdMs:930, critAdd:.05,desc:"Scales Arcane/Intel/Faith.",   defPen:0,  twin:false,stun:0,    focus:false },
  warhammer: { name:"Warhammer",  icon:"🔨",atkMul:1.40,spdMs:2200,critAdd:.06,desc:"-50% enemy DEF, 20% stun.",   defPen:.50,twin:false,stun:.20,  focus:false },
  twinblades:{ name:"Twin Blades",icon:"🗡🗡",atkMul:0.55,spdMs:500, critAdd:.08,desc:"Hits twice per interval.",    defPen:0,  twin:true, stun:0,    focus:false },
  bow:       { name:"Bow",        icon:"🏹",atkMul:0.70,spdMs:780, critAdd:.06,desc:"Every 3rd shot = 300% dmg.",  defPen:0,  twin:false,stun:0,    focus:true  },
};

const RARITY_WEIGHTS = { common:50, uncommon:30, rare:15, mythic:4, cursed:1 };

const RELICS = [
  { id:"lifeLeech",   name:"Bloodstone Shard",icon:"💎",desc:"Restore 8% damage as HP.",          rarity:"common"   },
  { id:"jailBurden",  name:"Jailer's Burden", icon:"⛓️",desc:"+2 ATK per Prisoner killed.",        rarity:"uncommon" },
  { id:"frostCore",   name:"Frost Core",       icon:"❄️",desc:"Frost +40% faster. Snap Freeze 2×.",rarity:"uncommon" },
  { id:"echoMirror",  name:"Echo Mirror",      icon:"🪞",desc:"30% chance to strike twice.",        rarity:"rare"     },
  { id:"soulAnchor",  name:"Soul Anchor",      icon:"⚓",desc:"+1 Soul per kill.",                  rarity:"rare"     },
  { id:"arcaneCoil",  name:"Arcane Coil",      icon:"🌀",desc:"Poison faster; poisoned = gold.",    rarity:"uncommon" },
  { id:"voidHeart",   name:"Void Heart",       icon:"🖤",desc:"50% damage dealt → HP.",             rarity:"mythic"   },
  { id:"misersTotem", name:"Miser's Totem",    icon:"🪙",desc:"+30% gold.",                         rarity:"common"   },
  { id:"burstCrystal",name:"Burst Crystal",    icon:"💥",desc:"Burst Strike +120% damage.",         rarity:"rare"     },
  { id:"consecGround",name:"Holy Mantle",      icon:"✝️",desc:"Faith heals 0.5% HP per enemy hit.",rarity:"rare"     },
  { id:"glacialHeart",name:"Glacial Heart",    icon:"🧊",desc:"Snap Freeze: +5 stacks each.",       rarity:"mythic"   },
  { id:"enchantEdge", name:"Enchanted Edge",   icon:"✨",desc:"Intel adds 0.8× to ATK (Spellblade).",rarity:"rare"   },
  { id:"cursedCoin",  name:"Cursed Coin",      icon:"💀",desc:"+200% Gold — lose 5 HP/sec.",        rarity:"cursed"   },
  { id:"bloodpact",   name:"Blood Pact",       icon:"🩸",desc:"+150% ATK — enemy DEF doubled.",     rarity:"cursed"   },
  { id:"soulhunger",  name:"Soul Hunger",      icon:"😤",desc:"+300% Souls — max HP halved.",       rarity:"cursed"   },
  { id:"berserker",   name:"Berserker's Rage", icon:"💢",desc:"+100%–200% ATK scaling with low HP.",rarity:"cursed"   },
];

const MILESTONE_RELICS = [
  { id:"warlordCrown",name:"Warlord's Crown",icon:"👑",desc:"+80% ATK, −25% max HP.",    rarity:"mythic",atkBonus:.80,hpPenalty:.25 },
  { id:"vampireFang", name:"Vampire Fang",  icon:"🦷",desc:"+50% leech, −20% DEF.",      rarity:"mythic",leechBonus:.50,defPenalty:.20 },
  { id:"soulPact",    name:"Soul Pact",     icon:"📜",desc:"×3 Souls, −30% gold.",        rarity:"mythic",soulMul:3,goldPenalty:.30 },
  { id:"glasscannon", name:"Glass Cannon",  icon:"💣",desc:"+120% ATK, DEF → 0.",         rarity:"mythic",atkBonus:1.20,zeroDef:true },
];

const ENEMY_POOL = [
  { type:"prisoner",name:"Prisoner",     icon:"👤",hpM:.80,atkM:.70, gold:8,  souls:1,defBypass:0 },
  { type:"specter", name:"Specter",      icon:"👻",hpM:.90,atkM:1.0,  gold:11, souls:1,defBypass:0 },
  { type:"golem",   name:"Stone Golem",  icon:"🗿",hpM:2.2, atkM:.85,  gold:22, souls:3,defBypass:0 },
  { type:"wraith",  name:"Spire Wraith", icon:"💀",hpM:1.2, atkM:1.5,  gold:16, souls:2,defBypass:0 },
  { type:"demon",   name:"Spire Demon",  icon:"😈",hpM:1.5, atkM:1.7,  gold:20, souls:3,defBypass:0 },
  { type:"mimic",   name:"Spire Mimic",  icon:"📦",hpM:1.0, atkM:2.2,  gold:35, souls:5,defBypass:0 },
  { type:"void",    name:"Void Elemental",icon:"🌀",hpM:1.3, atkM:1.4, gold:25, souls:4,defBypass:.40},
];
const ELITE_T={type:"elite",name:"Floor Guardian",icon:"👹",hpM:4.0,atkM:2.0,gold:90, souls:12,defBypass:0};
const BOSS_T ={type:"boss", name:"Spire Warden",  icon:"🐉",hpM:9.0,atkM:3.0,gold:230,souls:38,defBypass:0};

const BLESSINGS = [
  {id:"atkUp",     name:"+25% ATK",    icon:"⚔️",desc:"ATK ×1.25 this run."},
  {id:"hpRegen",   name:"Regeneration",icon:"💚",desc:"Restore 1.5% max HP/tick."},
  {id:"goldRush",  name:"Gold Rush",   icon:"💰",desc:"+60% gold from kills."},
  {id:"quickBurst",name:"Quick Burst", icon:"⚡",desc:"Burst Strike +80% dmg."},
  {id:"defUp",     name:"+20% DEF",    icon:"🛡️",desc:"DEF ×1.20 this run."},
  {id:"soulFeast", name:"Soul Feast",  icon:"💜",desc:"+5 souls/kill."},
  {id:"snapPower", name:"Snap+",       icon:"🧊",desc:"Snap Freeze at 70% frost."},
  {id:"holyGround",name:"Holy Ground", icon:"✝️",desc:"Faith aura +0.5% more."},
];

const PRESTIGE_SKILLS = [
  {id:"startWithRelic",name:"Fortune's Favour",icon:"🍀",desc:"Start every run with a relic.",baseCost:2},
  {id:"doubleSouls",   name:"Soul Amplifier",  icon:"💜",desc:"Double all Soul gains.",        baseCost:3},
  {id:"extraBlessing", name:"Blessed Climber", icon:"✨",desc:"Extra Blessing every 5 floors.",  baseCost:3},
  {id:"weaponMastery", name:"Weapon Mastery",  icon:"⚔️",desc:"+30% ATK on all weapons.",       baseCost:4},
  {id:"ironWill",      name:"Iron Will",       icon:"🛡️",desc:"Survive one killing blow.",       baseCost:5},
];

const FORGE_ITEMS = [
  {id:"wepDmg",    name:"Weapon Damage", icon:"⚔️",desc:"+9 ATK/lv",          maxLv:20,cost:lv=>Math.ceil(50*Math.pow(1.5,lv))},
  {id:"atkSpd",    name:"Attack Speed",  icon:"💨",desc:"-6% interval/lv",     maxLv:10,cost:lv=>Math.ceil(75*Math.pow(1.5,lv))},
  {id:"critChance",name:"Critical Edge", icon:"🎯",desc:"+4% crit/lv",         maxLv:10,cost:lv=>Math.ceil(90*Math.pow(1.5,lv))},
  {id:"critDmg",   name:"Crit Power",    icon:"💢",desc:"+15% crit mult/lv",   maxLv:10,cost:lv=>Math.ceil(110*Math.pow(1.5,lv))},
];
const RESEARCH_ITEMS = [
  {id:"baseHp",    name:"Iron Constitution",icon:"❤️",desc:"+25 HP/lv",        maxLv:10,cost:lv=>Math.ceil(40*Math.pow(1.5,lv))},
  {id:"baseAtk",   name:"Sharpened Edge",   icon:"🗡️",desc:"+4 ATK/lv",        maxLv:10,cost:lv=>Math.ceil(60*Math.pow(1.5,lv))},
  {id:"baseDef",   name:"Iron Skin",        icon:"🛡️",desc:"+3 DEF/lv",        maxLv:8, cost:lv=>Math.ceil(50*Math.pow(1.5,lv))},
  {id:"soulMult",  name:"Soul Harvest",     icon:"💜",desc:"+20% souls/lv",    maxLv:8, cost:lv=>Math.ceil(100*Math.pow(1.5,lv))},
  {id:"oraclePlus",name:"Oracle Mastery",   icon:"🔮",desc:"Unlock 2nd Oracle", maxLv:1, cost:()=>500},
];

const BIOMES = [
  {name:"Dungeon",     bg:"#07080f",accent:"#6366f1",fogC:"99,102,241"},
  {name:"Catacombs",   bg:"#0b0609",accent:"#f472b6",fogC:"244,114,182"},
  {name:"Crystal Cave",bg:"#040d13",accent:"#22d3ee",fogC:"34,211,238"},
  {name:"Infernal",    bg:"#0e0602",accent:"#f97316",fogC:"249,115,22"},
  {name:"The Void",    bg:"#05050a",accent:"#a78bfa",fogC:"167,139,250"},
];

// ═══════════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════════
const shuffle=a=>{const b=[...a];for(let i=b.length-1;i>0;i--){const j=0|Math.random()*(i+1);[b[i],b[j]]=[b[j],b[i]];}return b;};
const clamp=(v,lo,hi)=>Math.min(hi,Math.max(lo,v));
const pct=(a,b)=>b>0?clamp(0|a/b*100,0,100):0;
const fmtN=n=>n>=1e6?(n/1e6).toFixed(1)+"M":n>=1e3?(n/1e3).toFixed(1)+"k":String(0|n);
const dailySeed=()=>{const d=new Date();return d.getFullYear()*10000+(d.getMonth()+1)*100+d.getDate();};
const getBiome=f=>BIOMES[Math.min(BIOMES.length-1,0|(f/20))];
const rc=r=>({common:"#9ca3af",uncommon:"#34d399",rare:"#818cf8",mythic:"#f59e0b",cursed:"#dc2626"}[r]||"#fff");
const applyDef=(raw,def,defPen=0,defBypass=0)=>{
  const effDef=Math.max(0,def*(1-defPen)*(1-defBypass));
  return Math.max(1,Math.floor(raw*(100/(100+effDef))));
};
const capCrit=c=>Math.min(0.80,c);

// Weighted relic picker
function weightedRelicPick(pool,count=3){
  const weighted=[];
  pool.forEach(r=>{ const w=RARITY_WEIGHTS[r.rarity]||10; for(let i=0;i<w;i++)weighted.push(r); });
  const seen=new Set();
  const picks=[];
  const shuffled=shuffle(weighted);
  for(const r of shuffled){ if(!seen.has(r.id)){seen.add(r.id);picks.push(r);if(picks.length>=count)break;} }
  return picks;
}

function buildHero(cls,forge,research,weapon,prestigeSkills=[]){
  const base=CLASSES[cls], wep=WEAPONS[weapon]||WEAPONS.fists;
  const wepMul=prestigeSkills.includes("weaponMastery")?wep.atkMul*1.3:wep.atkMul;
  const maxHp=base.hp+(research.baseHp||0)*25;
  const atk=Math.floor((base.atk+(research.baseAtk||0)*4+(forge.wepDmg||0)*9)*wepMul);
  const def=base.def+(research.baseDef||0)*3;
  const spd=Math.max(280,wep.spdMs*(1-(forge.atkSpd||0)*0.06));
  const crit=capCrit(base.crit+wep.critAdd+(forge.critChance||0)*0.04);
  const critMul=1.9+(forge.critDmg||0)*0.15;
  const startRelics=[];
  if(prestigeSkills.includes("startWithRelic")){
    startRelics.push(weightedRelicPick(RELICS.filter(r=>r.rarity!=="cursed"),1)[0]);
  }
  if(cls==="spellblade"){
    startRelics.push(RELICS.find(r=>r.id==="enchantEdge"));
  }
  return{cls,weapon,maxHp,hp:maxHp,atk,def,spd,crit,critMul,
    arcane:base.arcane,intel:base.intel,faith:base.faith,
    relics:startRelics.filter(Boolean),blessings:[],prisonerKills:0,
    snapFreezeStacks:0,ironWillUsed:false,prestigeSkills,
    holyShield:cls==="paladin"?40+base.faith*3:0,
    holyShieldMax:cls==="paladin"?40+base.faith*3:0,
    holyShieldTimer:0,
    stealthHits:0,stealthReady:false,
    focusShots:0,
  };
}

function buildEnemy(floor,corruption=0){
  const isBoss=floor%50===0&&floor>0, isElite=!isBoss&&floor%10===0;
  // Rare mimic/void spawn
  let pool=[...ENEMY_POOL.slice(0,5)];
  if(Math.random()<0.06)pool=[ENEMY_POOL[5]]; // mimic
  else if(Math.random()<0.05)pool=[ENEMY_POOL[6]]; // void elemental
  const tmpl=isBoss?BOSS_T:isElite?ELITE_T:pool[0|Math.random()*pool.length];
  // Scaled difficulty: 0.15 base, bumped to 0.16 after floor 50
  const baseScale=floor>50?0.16:0.15;
  const scale=1+(floor-1)*baseScale+corruption*0.3;
  const maxHp=Math.max(8,0|32*tmpl.hpM*scale);
  const atk=Math.max(1,0|9*tmpl.atkM*scale);
  const def=Math.max(0,0|tmpl.hpM*scale*2);
  return{...tmpl,floor,maxHp,hp:maxHp,atk,def,
    poisonBuildup:0,frostBuildup:0,poisoned:false,frozen:false,frozenLeft:0,
    snapFreezeReady:false,stunned:false,stunTimer:0,isElite,isBoss};
}

function calcHeroDmg(hero,enemy,isSecondHit=false){
  const wep=WEAPONS[hero.weapon]||WEAPONS.fists;
  let dmg=hero.atk;
  // Spellblade intel scaling
  if(hero.cls==="spellblade"){
    const edgeBonus=hero.relics.find(r=>r.id==="enchantEdge")?0.8:0.4;
    dmg+=hero.intel*edgeBonus;
  }
  // Bow focus shot
  let isFocusShot=false;
  if(wep.focus&&hero.focusShots>=2){isFocusShot=true;}
  if(isFocusShot)dmg*=3;

  if(!isSecondHit&&hero.relics.find(r=>r.id==="echoMirror")&&Math.random()<.30)dmg*=2;
  if(enemy.snapFreezeReady||(hero.relics.find(r=>r.id==="frostCore")&&enemy.frozen))dmg*=2;
  if(hero.snapFreezeStacks>0)dmg*=(1+hero.snapFreezeStacks*0.5);
  // Berserker cursed relic
  const bsrk=hero.relics.find(r=>r.id==="berserker");
  if(bsrk){const hp=hero.hp/hero.maxHp;dmg*=hp<.20?3:2;}
  // Blood pact
  const bp=hero.relics.find(r=>r.id==="bloodpact");
  if(bp)dmg*=2.5;

  const crit=hero.stealthReady||Math.random()<hero.crit;
  if(crit)dmg*=hero.critMul;
  const effDef=bp?enemy.def*2:enemy.def||0;
  const final=applyDef(dmg,effDef,wep.defPen||0,enemy.defBypass||0);
  return{dmg:Math.max(1,final),crit,snapConsumed:enemy.snapFreezeReady,isFocusShot,stealth:hero.stealthReady};
}

// ═══════════════════════════════════════════════════════════════
// MICRO COMPONENTS
// ═══════════════════════════════════════════════════════════════
function AttackBar({prog,color,label,stunned}){
  return(
    <div style={{marginTop:5}}>
      <div style={{display:"flex",justifyContent:"space-between",fontSize:8,color:"#475569",marginBottom:2}}>
        <span>{label}</span><span style={{color:stunned?"#f59e0b":color}}>{stunned?"STUNNED":`${Math.round(prog)}%`}</span>
      </div>
      <div style={{height:4,background:"#0a0e1a",borderRadius:2,overflow:"hidden"}}>
        <div style={{width:`${stunned?100:prog}%`,height:"100%",background:stunned?"#f59e0b":color,
          borderRadius:2,transition:"width .12s linear",boxShadow:prog>88?`0 0 6px ${color}99`:"none"}}/>
      </div>
    </div>
  );
}

function UpgradeRow({item,level,currency,balance,onBuy,accent="#6366f1"}){
  const lv=level||0,cost=item.cost(lv),maxed=lv>=item.maxLv,can=balance>=cost&&!maxed;
  return(
    <div style={{background:"rgba(255,255,255,.035)",backdropFilter:"blur(10px)",
      border:`1px solid ${maxed?"#065f46":"rgba(99,102,241,.18)"}`,
      borderRadius:12,padding:"10px 12px",display:"flex",alignItems:"center",gap:10,marginBottom:7}}>
      <span style={{fontSize:20,minWidth:24}}>{item.icon}</span>
      <div style={{flex:1,minWidth:0}}>
        <div style={{fontWeight:"bold",color:"#e2e8f0",fontSize:12}}>{item.name}</div>
        <div style={{fontSize:10,color:"#64748b",marginTop:1}}>{item.desc}</div>
        <div style={{display:"flex",gap:6,alignItems:"center",marginTop:4}}>
          <div style={{flex:1,height:3,background:"#0a0e1a",borderRadius:2}}>
            <div style={{width:`${pct(lv,item.maxLv)}%`,height:"100%",background:maxed?"#34d399":accent,borderRadius:2,transition:"width .3s"}}/>
          </div>
          <span style={{fontSize:9,color:maxed?"#34d399":"#475569",minWidth:28}}>{maxed?"MAX":`${lv}/${item.maxLv}`}</span>
        </div>
      </div>
      <button onClick={onBuy} disabled={!can}
        onPointerDown={e=>!e.currentTarget.disabled&&(e.currentTarget.style.transform="scale(0.93)")}
        onPointerUp={e=>e.currentTarget.style.transform="scale(1)"}
        style={{padding:"9px 11px",borderRadius:9,fontSize:11,minHeight:44,minWidth:54,
          background:maxed?"#1e293b":can?`linear-gradient(135deg,${accent},${accent}cc)`:"#1e293b",
          color:can?"#fff":"#374151",border:"none",cursor:can?"pointer":"default",
          fontFamily:"inherit",fontWeight:"bold",transition:"all .15s"}}>
        {maxed?"✓":`${currency}${fmtN(cost)}`}
      </button>
    </div>
  );
}

function RelicChestReveal({choices,onChoose,onSkip}){
  const [phase,setPhase]=useState("closed");
  const [show,setShow]=useState(false);
  useEffect(()=>{
    const a=setTimeout(()=>setPhase("opening"),150);
    const b=setTimeout(()=>{setPhase("open");setShow(true);},950);
    return()=>{clearTimeout(a);clearTimeout(b);};
  },[]);
  const topR=choices.reduce((a,r)=>{const rank={common:0,uncommon:1,rare:2,mythic:3,cursed:4};return rank[r.rarity]>rank[a.rarity||"common"]?r:a;},{rarity:"common"});
  const beam=rc(topR.rarity);
  return(
    <div style={{position:"absolute",inset:0,background:"rgba(5,6,14,.97)",borderRadius:18,zIndex:30,
      display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center",padding:14}}>
      {phase!=="closed"&&<div style={{position:"absolute",top:0,left:"50%",transform:"translateX(-50%)",
        width:140,height:"65%",background:`linear-gradient(to bottom,${beam}55,transparent)`,
        borderRadius:"0 0 70px 70px",animation:"beamExpand .8s ease forwards",pointerEvents:"none"}}/>}
      <div style={{fontSize:52,marginBottom:8,
        animation:phase==="opening"?"chestPop .4s ease":phase==="open"?"chestLand .22s ease":"none",
        filter:phase!=="closed"?`drop-shadow(0 0 22px ${beam})`:"none",transition:"filter .4s"}}>
        {phase==="closed"?"📦":phase==="opening"?"📬":"📭"}
      </div>
      <div style={{fontSize:10,letterSpacing:4,color:beam,marginBottom:14,
        opacity:phase==="open"?1:0,transition:"opacity .4s",fontWeight:"bold"}}>RELIC VAULT</div>
      {show&&(
        <div style={{display:"flex",gap:9,flexWrap:"wrap",justifyContent:"center",marginBottom:12,animation:"relicsReveal .3s ease"}}>
          {choices.map(r=>{
            const g=rc(r.rarity),cursed=r.rarity==="cursed";
            return(
              <button key={r.id} onClick={()=>onChoose(r)}
                onPointerDown={e=>e.currentTarget.style.transform="scale(0.94)"}
                onPointerUp={e=>e.currentTarget.style.transform="scale(1.04)"}
                onPointerOut={e=>e.currentTarget.style.transform="scale(1)"}
                style={{background:cursed?`radial-gradient(ellipse at top,${g}33,#150000 70%)`:`radial-gradient(ellipse at top,${g}20,#0a0e1a 70%)`,
                  border:`2px solid ${g}`,borderRadius:14,padding:13,color:"#e2e8f0",
                  textAlign:"center",minWidth:100,maxWidth:120,cursor:"pointer",
                  boxShadow:`0 0 ${cursed?36:20}px ${g}${cursed?"66":"44"}`,fontFamily:"inherit",
                  transition:"all .18s",minHeight:44}}>
                <div style={{fontSize:26}}>{r.icon}</div>
                <div style={{fontSize:11,fontWeight:"bold",color:g,marginTop:4}}>{r.name}</div>
                <div style={{fontSize:9,color:g,textTransform:"uppercase",letterSpacing:1,marginTop:1}}>{r.rarity}</div>
                {cursed&&<div style={{fontSize:9,color:"#ef4444",marginTop:2}}>⚠️ CURSED</div>}
                <div style={{fontSize:9,color:"#94a3b8",marginTop:4,lineHeight:1.3}}>{r.desc}</div>
              </button>
            );
          })}
        </div>
      )}
      {show&&<button onClick={onSkip} style={{padding:"9px 20px",borderRadius:8,background:"#1e293b",color:"#64748b",fontSize:11,border:"1px solid #334155",cursor:"pointer",fontFamily:"inherit",minHeight:44}}>Skip</button>}
    </div>
  );
}

function OfflineModal({data,onClose}){
  return(
    <div style={{position:"fixed",inset:0,background:"rgba(0,0,0,.9)",zIndex:200,display:"flex",alignItems:"center",justifyContent:"center"}}>
      <div style={{background:"linear-gradient(135deg,#0f0820,#0d1628)",border:"2px solid #6366f1",borderRadius:20,padding:26,maxWidth:300,width:"90%",textAlign:"center",boxShadow:"0 0 50px #6366f155",animation:"slideUp .4s ease"}}>
        <div style={{fontSize:36,marginBottom:6}}>🌙</div>
        <div style={{fontSize:17,fontWeight:"bold",background:"linear-gradient(90deg,#c4b5fd,#818cf8)",WebkitBackgroundClip:"text",WebkitTextFillColor:"transparent",marginBottom:4}}>Welcome Back!</div>
        <div style={{fontSize:11,color:"#64748b",marginBottom:16}}>The Oracle kept climbing.</div>
        <div style={{display:"grid",gridTemplateColumns:"1fr 1fr 1fr",gap:8,marginBottom:18}}>
          {[{icon:"💰",l:"Gold",v:fmtN(data.gold)},{icon:"💜",l:"Souls",v:fmtN(data.souls)},{icon:"🏔️",l:"Floors",v:data.floors}].map(x=>(
            <div key={x.l} style={{background:"rgba(255,255,255,.04)",border:"1px solid #1e293b",borderRadius:10,padding:9}}>
              <div style={{fontSize:18}}>{x.icon}</div>
              <div style={{fontSize:15,fontWeight:"bold",color:"#e2e8f0",marginTop:2}}>{x.v}</div>
              <div style={{fontSize:9,color:"#64748b"}}>{x.l}</div>
            </div>
          ))}
        </div>
        <button onClick={onClose} style={{width:"100%",padding:"12px 0",borderRadius:10,background:"linear-gradient(135deg,#4f46e5,#7c3aed)",color:"#fff",fontSize:13,fontWeight:"bold",border:"none",cursor:"pointer",fontFamily:"inherit",minHeight:44}}>Claim ✨</button>
      </div>
    </div>
  );
}

// Ad Modal
function AdModal({onComplete,onClose}){
  const [phase,setPhase]=useState("loading"); // loading | playing | done
  const [countdown,setCountdown]=useState(5);
  useEffect(()=>{
    const t1=setTimeout(()=>setPhase("playing"),1800);
    return()=>clearTimeout(t1);
  },[]);
  useEffect(()=>{
    if(phase!=="playing")return;
    const iv=setInterval(()=>setCountdown(c=>{if(c<=1){clearInterval(iv);setPhase("done");return 0;}return c-1;}),1000);
    return()=>clearInterval(iv);
  },[phase]);
  return(
    <div style={{position:"fixed",inset:0,background:"rgba(0,0,0,.92)",zIndex:200,display:"flex",alignItems:"center",justifyContent:"center"}}>
      <div style={{background:"linear-gradient(135deg,#0f1428,#0a0e1a)",border:"2px solid #f59e0b",borderRadius:20,padding:26,maxWidth:300,width:"90%",textAlign:"center",boxShadow:"0 0 50px #f59e0b44",animation:"slideUp .3s ease"}}>
        {phase==="loading"&&(<><div style={{fontSize:36,marginBottom:8}}>📺</div><div style={{fontSize:14,color:"#fbbf24",fontWeight:"bold"}}>Loading Ad...</div><div style={{marginTop:12,height:4,background:"#1e293b",borderRadius:2,overflow:"hidden"}}><div style={{height:"100%",background:"#f59e0b",animation:"adLoad 1.8s linear forwards",borderRadius:2}}/></div></>)}
        {phase==="playing"&&(<><div style={{fontSize:36,marginBottom:8}}>🎬</div><div style={{fontSize:14,color:"#fbbf24",fontWeight:"bold",marginBottom:6}}>Simulated Ad</div><div style={{fontSize:26,color:"#fff",fontWeight:"bold",marginBottom:4}}>{countdown}s</div><div style={{fontSize:11,color:"#64748b"}}>Watching earns you The Oracle's Gift!</div></>)}
        {phase==="done"&&(<><div style={{fontSize:36,marginBottom:8}}>🎁</div><div style={{fontSize:14,color:"#34d399",fontWeight:"bold",marginBottom:6}}>Reward Ready!</div><div style={{fontSize:11,color:"#94a3b8",marginBottom:16}}>Choose your Oracle's Gift:</div>
          <div style={{display:"flex",gap:9,marginBottom:14}}>
            <button onClick={()=>onComplete("gold")} style={{flex:1,padding:"12px 6px",borderRadius:10,background:"linear-gradient(135deg,#78350f,#f59e0b)",color:"#fff",fontSize:12,fontWeight:"bold",border:"none",cursor:"pointer",fontFamily:"inherit",minHeight:44}}>+500 💰<br/><span style={{fontSize:9}}>Instant Gold</span></button>
            <button onClick={()=>onComplete("buff")} style={{flex:1,padding:"12px 6px",borderRadius:10,background:"linear-gradient(135deg,#065f46,#34d399)",color:"#fff",fontSize:12,fontWeight:"bold",border:"none",cursor:"pointer",fontFamily:"inherit",minHeight:44}}>+50% Gold<br/><span style={{fontSize:9}}>30 min buff</span></button>
          </div>
        </>)}
        {phase!=="done"&&<button onClick={onClose} style={{fontSize:10,color:"#475569",background:"none",border:"none",cursor:"pointer",textDecoration:"underline",fontFamily:"inherit"}}>Cancel</button>}
      </div>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════
// MAIN APP
// ═══════════════════════════════════════════════════════════════
const DEFAULT_SAVE = {
  gold:60,souls:0,ancPow:1,ribbons:0,ascensions:0,
  forge:{},research:{},prestige:[],
  bestFloor:0,totalRuns:0,dailyBest:0,
  riftUnlocked:false,corruption:0,
  selClass:"wanderer",selWeapon:"sword",
  oracle1:"balanced",oracle2:"relics",
  runHistory:[],
  lastAdWatch:0, adBuff:0,
};

export default function App(){
  // ── Load persisted state ──
  const saved = loadSave() || DEFAULT_SAVE;

  const [gold,        setGold]        = useState(saved.gold);
  const [souls,       setSouls]       = useState(saved.souls);
  const [ancPow,      setAncPow]      = useState(saved.ancPow);
  const [ribbons,     setRibbons]     = useState(saved.ribbons);
  const [ascensions,  setAscensions]  = useState(saved.ascensions);
  const [forge,       setForge]       = useState(saved.forge||{});
  const [research,    setResearch]    = useState(saved.research||{});
  const [prestige,    setPrestige]    = useState(saved.prestige||[]);
  const [bestFloor,   setBestFloor]   = useState(saved.bestFloor);
  const [totalRuns,   setTotalRuns]   = useState(saved.totalRuns);
  const [riftUnlocked,setRiftUnlocked]= useState(saved.riftUnlocked);
  const [corruption,  setCorruption]  = useState(saved.corruption);
  const [selClass,    setSelClass]    = useState(saved.selClass||"wanderer");
  const [selWeapon,   setSelWeapon]   = useState(saved.selWeapon||"sword");
  const [oracle1,     setOracle1]     = useState(saved.oracle1||"balanced");
  const [oracle2,     setOracle2]     = useState(saved.oracle2||"relics");
  const [dailyBest,   setDailyBest]   = useState(saved.dailyBest||0);
  const [runHistory,  setRunHistory]  = useState(saved.runHistory||[]);
  const [lastAdWatch, setLastAdWatch] = useState(saved.lastAdWatch||0);
  const [adBuff,      setAdBuff]      = useState(saved.adBuff||0); // ms timestamp when buff expires

  // UI
  const [screen,    setScreen]   = useState("hub"); // hub | run | ascend
  const [hubTab,    setHubTab]   = useState("expedition"); // bottom nav tab
  const [autoBurst, setAutoBurst]= useState(false);
  const [logFilter, setLogFilter]= useState("all");
  const [particlesOn,setParticlesOn]=useState(true);
  const [showAd,    setShowAd]   = useState(false);
  const [offlineData,setOfflineData]=useState(null);

  // Render trigger
  const [tick,setTick]=useState(0);
  const bump=useCallback(()=>setTick(t=>t+1),[]);

  // VFX state
  const [critFlash,  setCritFlash]   = useState(false);
  const [burstFlash, setBurstFlash]  = useState(false);
  const [heroShake,  setHeroShake]   = useState(false);
  const [enemyShake, setEnemyShake]  = useState(false);
  const [ironFlash,  setIronFlash]   = useState(false);
  const [lowHpPulse, setLowHpPulse]  = useState(false);

  // Game ref
  const G=useRef(null);
  const cTimer=useRef(null);
  const bTimer=useRef(null);
  const cursedTimer=useRef(null);
  const lastH=useRef(0);
  const lastE=useRef(0);
  const ftId=useRef(0);
  const pId=useRef(0);
  const autoBurstRef=useRef(false);
  const particlesRef=useRef(true);
  const bestRef=useRef(saved.bestFloor);
  const goldRef=useRef(saved.gold);
  const soulsRef=useRef(saved.souls);

  useEffect(()=>{autoBurstRef.current=autoBurst;},[autoBurst]);
  useEffect(()=>{particlesRef.current=particlesOn;},[particlesOn]);
  useEffect(()=>{bestRef.current=bestFloor;},[bestFloor]);
  useEffect(()=>{goldRef.current=gold;},[gold]);
  useEffect(()=>{soulsRef.current=souls;},[souls]);

  // Persist on important changes
  const saveAll=useCallback(()=>{
    writeSave({gold:goldRef.current,souls:soulsRef.current,ancPow,ribbons,ascensions,
      forge,research,prestige,bestFloor:bestRef.current,totalRuns,dailyBest,
      riftUnlocked,corruption,selClass,selWeapon,oracle1,oracle2,runHistory,
      lastAdWatch,adBuff});
  },[ancPow,ribbons,ascensions,forge,research,prestige,totalRuns,dailyBest,riftUnlocked,corruption,selClass,selWeapon,oracle1,oracle2,runHistory,lastAdWatch,adBuff]);

  useEffect(()=>{saveAll();},[forge,research,prestige,ascensions,bestFloor,totalRuns]);

  // Particles
  const [ptcls,setPtcls]=useState([]);
  const spawnP=useCallback((cx,cy,color,n=8,sz=5)=>{
    if(!particlesRef.current)return;
    const now=Date.now();
    const ps=Array.from({length:n},(_,i)=>{
      const a=Math.random()*Math.PI*2,spd=16+Math.random()*36;
      return{id:pId.current++,x:cx+Math.random()*16-8,y:cy+Math.random()*12-6,
        size:sz*(0.5+Math.random()),opacity:.9,color,dx:Math.cos(a)*spd,dy:Math.sin(a)*spd-12,dur:400+Math.random()*320};
    });
    setPtcls(p=>[...p.slice(-35),...ps]);
    // Clear old particles to prevent memory leak
    setTimeout(()=>setPtcls(p=>p.filter(x=>!ps.find(q=>q.id===x.id))),820);
  },[]);

  // Floating text
  const [floats,setFloats]=useState([]);
  const spawnFloat=useCallback((text,color="#fff",bx=55,big=false)=>{
    const id=ftId.current++;
    const x=bx+(Math.random()*16-8);
    setFloats(p=>[...p.slice(-14),{id,text,color,x,big}]);
    setTimeout(()=>setFloats(p=>p.filter(f=>f.id!==id)),1400);
  },[]);

  // FX helpers
  const doHeroShake=()=>{setHeroShake(true);setTimeout(()=>setHeroShake(false),180);};
  const doEnemyShake=()=>{setEnemyShake(true);setTimeout(()=>setEnemyShake(false),140);};
  const doCritFlash=()=>{setCritFlash(true);setTimeout(()=>setCritFlash(false),200);};
  const doBurstFlash=()=>{setBurstFlash(true);setTimeout(()=>setBurstFlash(false),300);};

  useEffect(()=>{
    const g=G.current;
    setLowHpPulse(!!(g?.hero&&g.phase==="fighting"&&g.hero.hp/g.hero.maxHp<0.20));
  },[tick]);

  // Offline
  const lastSeenRef=useRef(Date.now());
  useEffect(()=>{
    const e=Date.now()-lastSeenRef.current;
    if(e>180000){
      const fl=0|e/30000,og=fl*10,os=0|fl*0.6;
      setGold(g=>g+og);setSouls(s=>s+os);
      setOfflineData({gold:og,souls:os,floors:fl});
    }
    const iv=setInterval(()=>{lastSeenRef.current=Date.now();},60000);
    return()=>clearInterval(iv);
  // eslint-disable-next-line
  },[]);

  // Ad buff indicator
  const adBuffActive = adBuff > Date.now();
  const adBuffMins = adBuffActive ? Math.ceil((adBuff-Date.now())/60000) : 0;
  const adGoldMul = adBuffActive ? 1.5 : 1.0;
  const adReady = Date.now()-lastAdWatch > 300000; // 5 min cooldown

  // Stop timers
  const stopAll=useCallback(()=>{
    clearInterval(cTimer.current);clearInterval(bTimer.current);clearInterval(cursedTimer.current);
    cTimer.current=null;bTimer.current=null;cursedTimer.current=null;
  },[]);

  // Attack bar progress
  const heroBarPct=()=>{const g=G.current;if(!g?.hero)return 0;return pct(Date.now()-lastH.current,g.hero.spd);};
  const enemyBarPct=()=>{const g=G.current;if(!g?.enemy||g.enemy.stunned)return 0;return pct(Date.now()-lastE.current,1600);};

  // Combat log helper
  const addLog=useCallback((g,text,type="info")=>{
    g.combatLog.unshift({text,type,floor:g.floor});
    if(g.combatLog.length>60)g.combatLog.pop();
  },[]);

  // ═══════════════════════════════════════════════════════════════
  // START RUN
  // ═══════════════════════════════════════════════════════════════
  const startRun=useCallback((isDaily=false)=>{
    stopAll();
    let cls=selClass,path=oracle1,forceRelic=null;
    if(isDaily){
      const s=dailySeed();
      cls=Object.keys(CLASSES)[s%Object.keys(CLASSES).length];
      const paths=["balanced","gold","relics","elite"];
      path=paths[(s*7)%paths.length];
      forceRelic=RELICS.filter(r=>r.rarity!=="cursed")[(s*13)%RELICS.filter(r=>r.rarity!=="cursed").length];
    }
    const hero=buildHero(cls,forge,research,selWeapon,prestige);
    if(forceRelic&&!hero.relics.find(r=>r.id===forceRelic.id))hero.relics.push(forceRelic);
    const enemy=buildEnemy(1,corruption);
    G.current={
      hero,enemy,floor:1,runGold:0,runSouls:0,
      burstCharge:0,burstMax:6500,phase:"fighting",
      blessingChoices:null,relicChoices:null,milestoneRelic:null,
      combatLog:[{text:`✨ Entered the Spire — ${enemy.name} ${enemy.icon} awaits.`,type:"info",floor:1}],
      floorLog:[],killCount:0,
      ancPow,research:{...research},corruption,
      oraclePath:path,oracle2,oracleMastery:!!research.oraclePlus,
      isDaily,runStart:Date.now(),goldPerMin:0,soulsPerMin:0,adGoldMul,
    };
    lastH.current=Date.now()-hero.spd+100;
    lastE.current=Date.now()-1600+300;
    setScreen("run");setTotalRuns(t=>t+1);bump();
    cTimer.current=setInterval(combatTick,40);
    bTimer.current=setInterval(()=>{
      const g=G.current;if(!g||g.phase!=="fighting")return;
      g.burstCharge=Math.min(g.burstMax,g.burstCharge+65);
      if(autoBurstRef.current&&g.burstCharge>=g.burstMax)fireBurst();
      bump();
    },70);
    cursedTimer.current=setInterval(()=>{
      const g=G.current;if(!g||g.phase!=="fighting")return;
      if(g.hero.relics.find(r=>r.id==="cursedCoin"))g.hero.hp=Math.max(1,g.hero.hp-5);
      if(g.hero.relics.find(r=>r.id==="soulhunger"))g.hero.hp=Math.max(1,g.hero.hp-2);
      bump();
    },1000);
  // eslint-disable-next-line
  },[selClass,selWeapon,forge,research,corruption,ancPow,oracle1,oracle2,prestige,adGoldMul,stopAll]);

  // ═══════════════════════════════════════════════════════════════
  // COMBAT TICK
  // ═══════════════════════════════════════════════════════════════
  const combatTick=useCallback(()=>{
    const g=G.current;
    if(!g||g.phase!=="fighting")return;
    const now=Date.now();
    const{hero,enemy}=g;
    if(!hero||!enemy||hero.hp<=0)return;
    let dirty=false;

    if(now-lastH.current>=hero.spd){
      lastH.current=now;
      const wep=WEAPONS[hero.weapon]||WEAPONS.fists;
      // Shadowblade stealth
      if(hero.cls==="shadowblade"&&!hero.stealthReady){
        hero.stealthHits=(hero.stealthHits||0)+1;
        if(hero.stealthHits>=3){hero.stealthReady=true;hero.stealthHits=0;}
      }
      // Bow focus tracking
      if(wep.focus){
        if(hero.focusShots>=2)hero.focusShots=0; else hero.focusShots++;
      }

      const hits=wep.twin?2:1;
      let total=0,anyCrit=false;
      for(let h=0;h<hits;h++){
        const{dmg,crit,snapConsumed,isFocusShot,stealth}=calcHeroDmg(hero,enemy,h>0);
        enemy.hp=Math.max(0,enemy.hp-dmg);
        total+=dmg;if(crit)anyCrit=true;
        if(snapConsumed){enemy.snapFreezeReady=false;enemy.frozen=false;}
        if(stealth)hero.stealthReady=false;
        if(hero.snapFreezeStacks>0)hero.snapFreezeStacks=Math.max(0,hero.snapFreezeStacks-1);
        // Leech
        const vf=hero.relics.find(r=>r.id==="voidHeart");
        const ll=hero.relics.find(r=>r.id==="lifeLeech");
        const vfang=hero.relics.find(r=>r.id==="vampireFang");
        const lp=vf?.id?0.50:vfang?0.50+vfang.leechBonus:ll?0.08:0;
        if(lp>0)hero.hp=Math.min(hero.maxHp,hero.hp+0|dmg*lp);
        // Stun
        if(wep.stun>0&&Math.random()<wep.stun&&!enemy.stunned){
          enemy.stunned=true;enemy.stunTimer=2200;
          spawnFloat("💫 STUN!","#f59e0b",65);
          addLog(g,"⚡ Warhammer stun!","relic");
        }
        if(isFocusShot){spawnFloat("🏹 FOCUS!","#22d3ee",58);addLog(g,"🏹 Bow: Focus Shot!","big");}
      }

      spawnFloat(anyCrit?`💥 ${fmtN(total)}!`:`-${fmtN(total)}`,anyCrit?"#ffd700":"#f87171",64,anyCrit);
      doEnemyShake();
      if(anyCrit){doCritFlash();addLog(g,`💥 CRIT! ${fmtN(total)} damage!`,"big");}
      if(hero.cls==="shadowblade"&&hero.stealthReady){spawnFloat("🌑 STEALTH!","#a78bfa",50);addLog(g,"🌑 Stealth ready!","relic");}

      // Status: Poison
      if(!enemy.poisoned){
        const ab=hero.arcane*0.9+(hero.relics.find(r=>r.id==="arcaneCoil")?13:0);
        enemy.poisonBuildup=Math.min(100,enemy.poisonBuildup+ab*0.18);
        if(enemy.poisonBuildup>=100){enemy.poisoned=true;enemy.poisonBuildup=0;spawnFloat("☠️","#a3e635",68);spawnP(64,50,"#a3e635",10,4);addLog(g,"☠️ Poisoned!","relic");}
      }
      // Frost
      if(!enemy.frozen&&!enemy.snapFreezeReady){
        const fb=hero.arcane*0.5+hero.intel*0.4+(hero.relics.find(r=>r.id==="frostCore")?15:0);
        const thresh=hero.blessings.includes("snapPower")?70:100;
        enemy.frostBuildup=Math.min(100,enemy.frostBuildup+fb*0.13);
        if(enemy.frostBuildup>=thresh){
          enemy.frozen=true;enemy.frozenLeft=3;enemy.frostBuildup=0;enemy.snapFreezeReady=true;
          spawnFloat("❄️ FREEZE!","#93c5fd",60);spawnP(64,50,"#93c5fd",16,5);
          if(hero.relics.find(r=>r.id==="glacialHeart"))hero.snapFreezeStacks+=5;
          addLog(g,"❄️ Snap Freeze!","relic");
        }
      }
      if(enemy.poisoned){const pd=0|hero.atk*0.28;enemy.hp=Math.max(0,enemy.hp-pd);spawnP(66,54,"#a3e635",5,3);if(hero.relics.find(r=>r.id==="arcaneCoil"))g.runGold++;}
      dirty=true;
    }

    if(enemy.stunned){enemy.stunTimer-=40;if(enemy.stunTimer<=0){enemy.stunned=false;enemy.stunTimer=0;}}

    if(!enemy.frozen&&!enemy.stunned&&enemy.hp>0&&now-lastE.current>=1600){
      lastE.current=now;
      let edmg=applyDef(enemy.atk,hero.def,0,0);
      if(hero.cls==="paladin"&&hero.holyShield>0){
        const ab=Math.min(hero.holyShield,edmg);edmg=Math.max(0,edmg-ab);
        hero.holyShield=Math.max(0,hero.holyShield-ab);
        if(ab>0)spawnFloat(`🛡️-${fmtN(ab)}`,"#fde68a",38);
      }
      if(hero.hp-edmg<=0&&hero.prestigeSkills?.includes("ironWill")&&!hero.ironWillUsed){
        edmg=hero.hp-1;hero.ironWillUsed=true;
        setIronFlash(true);setTimeout(()=>setIronFlash(false),900);
        spawnFloat("🛡️ IRON WILL!","#fbbf24",45,true);spawnP(38,55,"#fbbf24",20,7);
        addLog(g,"🛡️ Iron Will!","big");
      }
      hero.hp=Math.max(0,hero.hp-edmg);
      if(edmg>0){spawnFloat(`-${fmtN(edmg)}`,"#f87171",34);spawnP(34,48,"#ef4444",7,4);doHeroShake();}
      if(hero.blessings.includes("hpRegen"))hero.hp=Math.min(hero.maxHp,hero.hp+0|(hero.maxHp*0.015));
      if(hero.faith>=10||hero.relics.find(r=>r.id==="consecGround")){
        const hh=0|(hero.maxHp*(0.005+(hero.blessings.includes("holyGround")?0.005:0)));
        hero.hp=Math.min(hero.maxHp,hero.hp+hh);
        const hd=0|(hero.faith*0.35);enemy.hp=Math.max(0,enemy.hp-hd);
        if(hd>0){spawnP(62,60,"#fde68a",5,3);spawnFloat(`✝️${hd}`,"#fde68a",70);}
      }
      if(hero.cls==="paladin"){
        hero.holyShieldTimer=(hero.holyShieldTimer||0)+1600;
        if(hero.holyShieldTimer>=10000){hero.holyShield=hero.holyShieldMax;hero.holyShieldTimer=0;spawnFloat("🛡️ Recharged!","#fde68a",42);addLog(g,"🛡️ Holy Shield recharged!","relic");}
      }
      if(enemy.frozen){enemy.frozenLeft--;if(enemy.frozenLeft<=0)enemy.frozen=false;}
      dirty=true;
    }

    if(!dirty)return;
    const em=(Date.now()-g.runStart)/60000;
    if(em>0){g.goldPerMin=0|g.runGold/em;g.soulsPerMin=+(g.runSouls/em).toFixed(1);}
    if(enemy.hp<=0){onEnemyKilled();return;}
    if(hero.hp<=0){onHeroDead();return;}
    bump();
  // eslint-disable-next-line
  },[spawnFloat,spawnP,addLog]);

  // ═══════════════════════════════════════════════════════════════
  // ENEMY KILLED
  // ═══════════════════════════════════════════════════════════════
  const onEnemyKilled=()=>{
    const g=G.current;const{hero,enemy}=g;
    const ap=g.ancPow,res=g.research;
    const sM=(1+(res.soulMult||0)*.20)*(prestige.includes("doubleSouls")?2:1)*(hero.relics.find(r=>r.id==="soulhunger")?4:1);
    const sp=hero.relics.find(r=>r.id==="soulPact");
    const cc=hero.relics.find(r=>r.id==="cursedCoin");
    const gM=(hero.blessings.includes("goldRush")?1.6:1)*(hero.relics.find(r=>r.id==="misersTotem")?1.3:1)*(sp?.goldPenalty?1-sp.goldPenalty:1)*(cc?3:1)*g.adGoldMul;
    const sB=(hero.blessings.includes("soulFeast")?5:0)+(hero.relics.find(r=>r.id==="soulAnchor")?1:0)*(sp?.soulMul||1);
    const eg=Math.max(1,0|enemy.gold*gM*ap);
    const es=Math.max(0,0|(enemy.souls+sB)*sM*ap);
    g.runGold+=eg;g.runSouls+=es;g.killCount++;
    if(enemy.type==="prisoner"&&hero.relics.find(r=>r.id==="jailBurden")){hero.prisonerKills++;hero.atk+=2;}
    // Mimic surprise
    if(enemy.type==="mimic")addLog(g,"📦 The Mimic reveals itself!","big");
    g.floorLog.unshift(`⚔️ F${enemy.floor}: ${enemy.name} +${fmtN(eg)}💰 +${fmtN(es)}💜`);
    if(g.floorLog.length>60)g.floorLog.pop();
    const next=enemy.floor+1;
    g.floor=next;
    if(next>bestRef.current){bestRef.current=next;setBestFloor(next);writeSave({bestFloor:next});}
    if(g.isDaily&&next>dailyBest)setDailyBest(next);
    if(next>=100){setRiftUnlocked(true);}
    if(enemy.floor%10===0){
      g.milestoneRelic=shuffle([...MILESTONE_RELICS])[0];g.enemy=null;g.phase="milestone";
      addLog(g,"👑 Boss Relic!","big");bump();return;
    }
    if(prestige.includes("extraBlessing")&&enemy.floor%5===0&&enemy.floor%10!==0){
      g.blessingChoices=shuffle([...BLESSINGS]).slice(0,3);g.enemy=null;g.phase="blessing";
      addLog(g,"✨ Prestige Blessing!","relic");bump();return;
    }
    const bV=g.oraclePath==="relics"?0.28:(g.oracleMastery&&g.oracle2==="relics"?0.18:0.09);
    const vC=enemy.isElite?bV+0.20:bV;
    if(Math.random()<vC){
      const pool=RELICS.filter(r=>!hero.relics.find(rr=>rr.id===r.id));
      const picks=weightedRelicPick(pool,3);
      if(picks.length>0){g.relicChoices=picks;g.enemy=null;g.phase="relic";addLog(g,"🎁 Relic Vault!","relic");bump();return;}
    }
    spawnFloor(next);
  };

  const onHeroDead=()=>{
    const g=G.current;g.phase="dead";addLog(g,"💀 Fallen.","big");stopAll();
    const rec={floor:g.floor,kills:g.killCount,gold:0|g.runGold,souls:0|g.runSouls,
      cls:g.hero.cls,weapon:g.hero.weapon,relics:g.hero.relics.map(r=>r.name),date:new Date().toLocaleDateString()};
    setRunHistory(h=>{const nh=[rec,...h].slice(0,5);writeSave({runHistory:nh});return nh;});
    bump();
  };

  const spawnFloor=(fl)=>{
    const g=G.current;const ne=buildEnemy(fl,g.corruption);
    g.enemy=ne;g.phase="fighting";
    lastH.current=Date.now()-g.hero.spd+150;
    lastE.current=Date.now()-1600+320;
    addLog(g,`🏔️ Floor ${fl}: ${ne.name} ${ne.icon} (${ne.maxHp} HP)`,"info");bump();
  };

  // ── ACTIONS ──
  const fireBurst=()=>{
    const g=G.current;if(!g||g.phase!=="fighting"||g.burstCharge<g.burstMax)return;
    const{hero,enemy}=g;
    const dmg=0|hero.atk*4.5*(hero.blessings.includes("quickBurst")?1.8:1)*(hero.relics.find(r=>r.id==="burstCrystal")?2.2:1);
    enemy.hp=Math.max(0,enemy.hp-dmg);g.burstCharge=0;
    doBurstFlash();doCritFlash();
    spawnFloat(`⚡ ${fmtN(dmg)}!!`,"#fbbf24",50,true);spawnP(60,50,"#fbbf24",26,10);
    addLog(g,`⚡ Burst Strike! ${fmtN(dmg)} dmg!`,"big");
    if(enemy.hp<=0){onEnemyKilled();return;}bump();
  };

  const chooseBlessing=b=>{
    const g=G.current;g.hero.blessings.push(b.id);
    if(b.id==="atkUp")g.hero.atk=0|g.hero.atk*1.25;
    if(b.id==="defUp")g.hero.def=0|g.hero.def*1.20;
    g.blessingChoices=null;addLog(g,`✨ Blessing: ${b.name}!`,"relic");spawnFloor(g.floor);
  };
  const chooseRelic=r=>{const g=G.current;g.hero.relics.push(r);g.relicChoices=null;spawnFloat(`🎁 ${r.name}!`,rc(r.rarity));addLog(g,`🎁 ${r.name}!`,"relic");spawnFloor(g.floor);};
  const skipRelic=()=>{const g=G.current;g.relicChoices=null;spawnFloor(g.floor);};
  const chooseMilestone=r=>{
    const g=G.current;
    if(r.atkBonus)g.hero.atk=0|g.hero.atk*(1+r.atkBonus);
    if(r.hpPenalty){g.hero.maxHp=Math.max(10,0|g.hero.maxHp*(1-r.hpPenalty));g.hero.hp=Math.min(g.hero.hp,g.hero.maxHp);}
    if(r.zeroDef)g.hero.def=0;if(r.defPenalty)g.hero.def=Math.max(0,0|g.hero.def*(1-r.defPenalty));
    g.hero.relics.push(r);g.milestoneRelic=null;g.blessingChoices=shuffle([...BLESSINGS]).slice(0,3);g.phase="blessing";
    spawnFloat(`👑 ${r.name}!`,"#f59e0b");addLog(g,`👑 ${r.name}!`,"big");bump();
  };
  const skipMilestone=()=>{const g=G.current;g.milestoneRelic=null;g.blessingChoices=shuffle([...BLESSINGS]).slice(0,3);g.phase="blessing";bump();};

  const returnToHub=()=>{
    stopAll();
    const g=G.current;
    if(g){
      setGold(v=>{const n=v+0|g.runGold;goldRef.current=n;return n;});
      setSouls(v=>{const n=v+0|g.runSouls;soulsRef.current=n;return n;});
      if(g.floor>=100)setRibbons(r=>r+1);
    }
    G.current=null;setScreen("hub");saveAll();
  };

  const buyForge=item=>{
    const lv=forge[item.id]||0;if(lv>=item.maxLv||gold<item.cost(lv))return;
    const newG=gold-item.cost(lv);setGold(newG);goldRef.current=newG;
    const nf={...forge,[item.id]:lv+1};setForge(nf);writeSave({gold:newG,forge:nf});
  };
  const buyResearch=item=>{
    const lv=research[item.id]||0;if(lv>=item.maxLv||souls<item.cost(lv))return;
    const ns=souls-item.cost(lv);setSouls(ns);soulsRef.current=ns;
    const nr={...research,[item.id]:lv+1};setResearch(nr);writeSave({souls:ns,research:nr});
  };
  const buyPrestige=sk=>{
    const dCost=sk.baseCost+prestige.length;
    if(prestige.includes(sk.id)||ribbons<dCost)return;
    setRibbons(r=>r-dCost);setPrestige(p=>[...p,sk.id]);
  };
  const doAscend=()=>{
    setAncPow(a=>a*2.5);setAscensions(a=>a+1);
    const newG=60,newS=0;setGold(newG);setSouls(newS);goldRef.current=newG;soulsRef.current=newS;
    setForge({});setResearch({});setScreen("hub");saveAll();
  };

  const handleAdReward=(choice)=>{
    if(choice==="gold"){setGold(g=>{const n=g+500;goldRef.current=n;return n;});}
    else{setAdBuff(Date.now()+30*60*1000);}
    setLastAdWatch(Date.now());setShowAd(false);saveAll();
  };

  // Derived
  const g=G.current;
  const hero=g?.hero;
  const enemy=g?.enemy;
  const burstReady=!!(g&&g.burstCharge>=g.burstMax);
  const heroHpPct=pct(Math.max(0,hero?.hp||0),hero?.maxHp||1);
  const biome=getBiome(g?.floor||0);
  const filteredLog=((g?.combatLog)||[]).filter(e=>logFilter==="all"?true:logFilter==="big"?e.type==="big":e.type==="relic").slice(0,8);

  const stars=useRef(Array.from({length:50},()=>({x:Math.random()*100,y:Math.random()*100,s:.3+Math.random()*1.8,d:1.5+Math.random()*3.5,o:.06+Math.random()*.4}))).current;

  const glass=(extra={})=>({
    background:"rgba(10,14,26,0.82)",backdropFilter:"blur(14px)",WebkitBackdropFilter:"blur(14px)",
    border:"1px solid rgba(99,102,241,0.18)",borderRadius:14,...extra,
  });

  // ═══════════════════════════════════════════════════════════════
  // BOTTOM NAV TABS
  // ═══════════════════════════════════════════════════════════════
  const NAV=[{id:"expedition",icon:"⚔️",label:"Expedition"},{id:"forge",icon:"🔨",label:"Forge"},
             {id:"research",icon:"🔬",label:"Research"},{id:"prestige",icon:"🎀",label:"Astral"},
             {id:"rift",icon:"🌌",label:"Rift"}];

  // ═══════════════════════════════════════════════════════════════
  // RENDER
  // ═══════════════════════════════════════════════════════════════
  return(
    // Outer wrapper — centers the phone bezel on large screens
    <div style={{minHeight:"100vh",background:"#02030a",display:"flex",alignItems:"flex-start",justifyContent:"center"}}>
      {/* Phone bezel container */}
      <div style={{
        width:"100%",maxWidth:450,minHeight:"100vh",
        background:screen==="run"&&g?biome.bg:"#07080f",
        color:"#e2e8f0",fontFamily:"'Palatino Linotype','Book Antiqua',Palatino,serif",
        position:"relative",overflow:"hidden",
        boxShadow:"0 0 0 1px #1e293b, 0 0 60px rgba(0,0,0,.8), 0 25px 60px rgba(0,0,0,.5)",
        transition:"background 1.5s ease",display:"flex",flexDirection:"column",
      }}>
        {/* Stars */}
        <div style={{position:"fixed",inset:0,pointerEvents:"none",zIndex:0,maxWidth:450,margin:"0 auto"}}>
          {stars.map((s,i)=>(
            <div key={i} style={{position:"absolute",left:`${s.x}%`,top:`${s.y}%`,width:s.s,height:s.s,
              borderRadius:"50%",background:"#fff",opacity:s.o,animation:`twinkle ${s.d}s ease-in-out infinite alternate`}}/>
          ))}
        </div>

        {/* Vignette */}
        <div style={{position:"fixed",inset:0,pointerEvents:"none",zIndex:0,
          background:"radial-gradient(ellipse at 50% 50%, transparent 30%, rgba(0,0,0,.68) 100%)",maxWidth:450}}/>

        {/* Biome fog */}
        {screen==="run"&&g&&<div style={{position:"fixed",top:0,left:0,right:0,bottom:0,maxWidth:450,margin:"0 auto",
          pointerEvents:"none",zIndex:0,background:`rgba(${biome.fogC},0.05)`,transition:"background 1.5s"}}/>}

        {/* Parallax background bars (simulate tower scrolling) */}
        {screen==="run"&&g&&(
          <div style={{position:"fixed",left:0,right:0,top:0,bottom:0,maxWidth:450,margin:"0 auto",
            pointerEvents:"none",zIndex:0,overflow:"hidden"}}>
            {Array.from({length:6},(_,i)=>(
              <div key={i} style={{
                position:"absolute",left:0,right:0,
                height:2,background:biome.accent,opacity:0.03+i*0.015,
                top:`${((g.floor*(i+1)*23)%100)}%`,
                transition:"top 0.8s ease",
              }}/>
            ))}
          </div>
        )}

        {/* Crit/Burst flash overlays */}
        {critFlash&&<div style={{position:"fixed",inset:0,maxWidth:450,background:"rgba(255,220,50,0.18)",pointerEvents:"none",zIndex:50,animation:"quickFlash .2s ease forwards"}}/>}
        {burstFlash&&<div style={{position:"fixed",inset:0,maxWidth:450,background:"rgba(245,158,11,0.25)",pointerEvents:"none",zIndex:50,animation:"quickFlash .3s ease forwards"}}/>}
        {ironFlash&&<div style={{position:"fixed",inset:0,maxWidth:450,background:"rgba(251,191,36,0.3)",pointerEvents:"none",zIndex:50,animation:"quickFlash .9s ease forwards"}}/>}
        {lowHpPulse&&<div style={{position:"fixed",inset:0,maxWidth:450,pointerEvents:"none",zIndex:1,background:"rgba(239,68,68,.07)",animation:"heartbeat 1s ease infinite"}}/>}

        <style>{`
          @keyframes twinkle{from{opacity:.01}to{opacity:.9}}
          @keyframes quickFlash{0%{opacity:1}100%{opacity:0}}
          @keyframes heartbeat{0%,100%{opacity:.05}50%{opacity:.18}}
          @keyframes slideUp{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:translateY(0)}}
          @keyframes particleDrift{0%{opacity:1;transform:translate(-50%,-50%) scale(1)}100%{opacity:0;transform:translate(calc(-50% + var(--dx)),calc(-50% + var(--dy))) scale(0.1)}}
          @keyframes floatUp{0%{opacity:1;transform:translateX(-50%) translateY(0)}100%{opacity:0;transform:translateX(-50%) translateY(-62px)}}
          @keyframes floatBig{0%{opacity:1;transform:translateX(-50%) translateY(0) scale(1.3)}100%{opacity:0;transform:translateX(-50%) translateY(-80px) scale(1)}}
          @keyframes shakeN{0%,100%{transform:translateX(0)}25%{transform:translateX(-4px)}75%{transform:translateX(4px)}}
          @keyframes pulseHP{0%,100%{transform:scaleY(1)}50%{transform:scaleY(1.08)}}
          @keyframes burstPulse{0%,100%{box-shadow:0 0 14px #f59e0b88}50%{box-shadow:0 0 38px #fbbf24cc}}
          @keyframes milestoneGlow{0%,100%{box-shadow:0 0 22px #f59e0b44}50%{box-shadow:0 0 60px #f59e0bcc}}
          @keyframes beamExpand{0%{opacity:0;height:0}35%{opacity:1}100%{opacity:0}}
          @keyframes relicsReveal{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:translateY(0)}}
          @keyframes chestPop{0%{transform:scale(1)rotate(0)}30%{transform:scale(1.18)rotate(-9deg)}65%{transform:scale(.93)rotate(5deg)}100%{transform:scale(1)rotate(0)}}
          @keyframes chestLand{0%{transform:scale(1.22)}100%{transform:scale(1)}}
          @keyframes adLoad{from{width:0}to{width:100%}}
          @keyframes ironFlashAnim{0%{opacity:1}100%{opacity:0}}
          .btn{cursor:pointer;border:none;font-family:inherit;transition:all .15s;min-height:44px;touch-action:manipulation;}
          .btn:active{transform:scale(0.94)!important;}
          .hpfill{border-radius:6px;transition:width 0.3s ease-out;}
          ::-webkit-scrollbar{width:3px}::-webkit-scrollbar-track{background:#0a0e1a}::-webkit-scrollbar-thumb{background:#334155;border-radius:2px}
        `}</style>

        {offlineData&&<OfflineModal data={offlineData} onClose={()=>setOfflineData(null)}/>}
        {showAd&&<AdModal onComplete={handleAdReward} onClose={()=>setShowAd(false)}/>}

        {/* ── PERSISTENT TOP INFO BAR ── */}
        <div style={{position:"sticky",top:0,zIndex:40,
          background:"rgba(7,8,15,0.92)",backdropFilter:"blur(16px)",
          borderBottom:"1px solid rgba(99,102,241,0.2)",padding:"8px 14px",
          display:"flex",alignItems:"center",justifyContent:"space-between",flexShrink:0}}>
          <div style={{display:"flex",gap:14,alignItems:"center"}}>
            <span style={{fontSize:13,color:"#fbbf24",fontWeight:"bold"}}>💰{fmtN(gold)}</span>
            <span style={{fontSize:13,color:"#a78bfa",fontWeight:"bold"}}>💜{fmtN(souls)}</span>
            <span style={{fontSize:13,color:"#34d399",fontWeight:"bold"}}>🎀{ribbons}</span>
          </div>
          <div style={{display:"flex",gap:8,alignItems:"center"}}>
            {adBuffActive&&<span style={{fontSize:9,color:"#fbbf24",background:"rgba(245,158,11,.15)",padding:"2px 6px",borderRadius:4}}>+50%💰 {adBuffMins}m</span>}
            <span style={{fontSize:10,color:"#6366f1",fontVariant:"small-caps",letterSpacing:1}}>
              {screen==="run"&&g?`F${g.floor} · ${biome.name}`:"Echoes"}
            </span>
            <button onClick={()=>setParticlesOn(p=>!p)} style={{fontSize:9,padding:"2px 5px",borderRadius:4,border:`1px solid #334155`,background:"transparent",color:"#475569",cursor:"pointer",fontFamily:"inherit",minHeight:0}}>
              {particlesOn?"✨":"○"}
            </button>
          </div>
        </div>

        {/* ── SCROLLABLE CONTENT ── */}
        <div style={{flex:1,overflowY:"auto",padding:"10px 12px",paddingBottom:screen==="hub"?80:12,position:"relative",zIndex:2}}>

          {/* ════════ HUB CONTENT ════════ */}
          {screen==="hub"&&(
            <div style={{animation:"slideUp .28s ease"}}>
              {/* Oracle's Gift / Ad button */}
              <div style={{...glass({border:"1px solid rgba(245,158,11,.3)"}),padding:11,marginBottom:10,display:"flex",alignItems:"center",gap:10}}>
                <div style={{fontSize:26}}>🎁</div>
                <div style={{flex:1}}>
                  <div style={{fontWeight:"bold",color:"#fbbf24",fontSize:12}}>The Oracle's Gift</div>
                  <div style={{fontSize:10,color:"#64748b",marginTop:1}}>{adBuffActive?`+50% Gold active — ${adBuffMins}m remaining`:adReady?"Watch an ad for +500 Gold or 30min Gold Buff":"Available in a few minutes"}</div>
                </div>
                <button className="btn" onClick={()=>adReady&&setShowAd(true)} disabled={!adReady||adBuffActive}
                  style={{padding:"9px 12px",borderRadius:9,fontSize:11,fontWeight:"bold",
                    background:adReady&&!adBuffActive?"linear-gradient(135deg,#78350f,#f59e0b)":"#1e293b",
                    color:adReady&&!adBuffActive?"#fff":"#374151",border:"none",minHeight:44}}>
                  {adBuffActive?"Active":adReady?"Watch":"Wait"}
                </button>
              </div>

              {/* EXPEDITION */}
              {hubTab==="expedition"&&(<div>
                <p style={{textAlign:"center",fontSize:11,color:"#64748b",marginBottom:9}}>Choose class and begin the climb.</p>
                <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:7,marginBottom:10}}>
                  {Object.entries(CLASSES).map(([id,cls])=>{
                    const act=selClass===id;
                    return(
                      <div key={id} onClick={()=>setSelClass(id)}
                        onPointerDown={e=>e.currentTarget.style.transform="scale(0.96)"}
                        onPointerUp={e=>e.currentTarget.style.transform="scale(1)"}
                        style={{...glass(),padding:10,cursor:"pointer",transition:"all .18s",
                          border:`2px solid ${act?"#6366f1":"rgba(99,102,241,.12)"}`,
                          boxShadow:act?"0 0 12px #6366f1,0 0 24px #6366f133":"none"}}>
                        <div style={{fontSize:20}}>{cls.icon}</div>
                        <div style={{fontWeight:"bold",fontSize:11,color:"#c4b5fd",marginTop:2}}>{cls.name}</div>
                        <div style={{fontSize:9,color:"#64748b",marginTop:2,lineHeight:1.3}}>{cls.desc}</div>
                        <div style={{display:"flex",gap:5,marginTop:4,fontSize:9}}>
                          <span style={{color:"#f87171"}}>❤️{cls.hp}</span>
                          <span style={{color:"#fbbf24"}}>⚔️{cls.atk}</span>
                          <span style={{color:"#60a5fa"}}>🛡️{cls.def}</span>
                        </div>
                      </div>
                    );
                  })}
                </div>
                {/* Weapon selector */}
                <div style={{...glass(),padding:10,marginBottom:9}}>
                  <div style={{fontSize:9,color:"#6366f1",letterSpacing:2,marginBottom:7}}>⚔️ WEAPON</div>
                  <div style={{display:"grid",gridTemplateColumns:"repeat(4,1fr)",gap:5,marginBottom:6}}>
                    {Object.entries(WEAPONS).slice(0,4).map(([id,w])=>(
                      <div key={id} onClick={()=>setSelWeapon(id)} style={{
                        background:selWeapon===id?"rgba(99,102,241,.22)":"rgba(255,255,255,.03)",
                        border:`1px solid ${selWeapon===id?"#6366f1":"#1e293b"}`,
                        borderRadius:8,padding:"7px 4px",cursor:"pointer",textAlign:"center",
                        boxShadow:selWeapon===id?"0 0 8px #6366f1":""
                      }}>
                        <div style={{fontSize:18}}>{w.icon}</div>
                        <div style={{fontSize:8,color:"#94a3b8",marginTop:2}}>{w.name}</div>
                      </div>
                    ))}
                  </div>
                  <div style={{display:"grid",gridTemplateColumns:"repeat(4,1fr)",gap:5}}>
                    {Object.entries(WEAPONS).slice(4).map(([id,w])=>(
                      <div key={id} onClick={()=>setSelWeapon(id)} style={{
                        background:selWeapon===id?"rgba(99,102,241,.22)":"rgba(255,255,255,.03)",
                        border:`1px solid ${selWeapon===id?"#6366f1":"#1e293b"}`,
                        borderRadius:8,padding:"7px 4px",cursor:"pointer",textAlign:"center",
                        boxShadow:selWeapon===id?"0 0 8px #6366f1":""
                      }}>
                        <div style={{fontSize:18}}>{w.icon}</div>
                        <div style={{fontSize:8,color:"#94a3b8",marginTop:2}}>{w.name}</div>
                      </div>
                    ))}
                  </div>
                  {selWeapon&&<div style={{marginTop:7,fontSize:10,color:"#64748b"}}>{WEAPONS[selWeapon]?.desc}</div>}
                </div>
                {/* Oracle */}
                <div style={{...glass(),padding:10,marginBottom:9}}>
                  <div style={{fontSize:9,color:"#6366f1",letterSpacing:2,marginBottom:7}}>🔮 ORACLE PATH</div>
                  <div style={{display:"grid",gridTemplateColumns:"repeat(4,1fr)",gap:5}}>
                    {[{id:"balanced",i:"⚖️",n:"Balanced"},{id:"gold",i:"💰",n:"Gold"},{id:"relics",i:"🎁",n:"Relics"},{id:"elite",i:"👹",n:"Elites"}].map(p=>(
                      <div key={p.id} onClick={()=>setOracle1(p.id)} style={{background:oracle1===p.id?"rgba(99,102,241,.2)":"transparent",border:`1px solid ${oracle1===p.id?"#6366f1":"#1e293b"}`,borderRadius:8,padding:7,cursor:"pointer",textAlign:"center"}}>
                        <span style={{fontSize:14}}>{p.i}</span><div style={{fontSize:8,color:"#94a3b8",marginTop:2}}>{p.n}</div>
                      </div>
                    ))}
                  </div>
                </div>
                {corruption>0&&<div style={{...glass({border:"1px solid rgba(124,58,237,.4)"}),padding:8,marginBottom:9,fontSize:11,color:"#c4b5fd"}}>☠️ Corruption {corruption}: +{corruption*30}% harder · +{corruption*25}% loot</div>}
                <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:8,marginBottom:8}}>
                  <button className="btn" onClick={()=>startRun(false)} style={{borderRadius:12,background:"linear-gradient(135deg,#4f46e5,#7c3aed)",color:"#fff",fontSize:13,fontWeight:"bold",boxShadow:"0 4px 20px #4f46e555"}}>⚔️ Expedition</button>
                  <button className="btn" onClick={()=>startRun(true)} style={{borderRadius:12,background:"linear-gradient(135deg,#065f46,#059669)",color:"#fff",fontSize:12,fontWeight:"bold"}}>📅 Daily<br/><span style={{fontSize:9,opacity:.7}}>Best F{dailyBest}</span></button>
                </div>
                {totalRuns>=3&&souls>=400&&<button className="btn" onClick={()=>setScreen("ascend")} style={{width:"100%",borderRadius:12,background:"linear-gradient(135deg,#78350f,#b45309)",color:"#fef3c7",fontSize:11,fontWeight:"bold",marginBottom:8}}>🌅 Ascend — ×2.5 Ancient Power</button>}
                <div style={{...glass(),padding:9,display:"grid",gridTemplateColumns:"1fr 1fr",gap:4,fontSize:10,color:"#64748b"}}>
                  <span>Runs: {totalRuns}</span><span>Best: F{bestFloor}</span>
                  <span>Ascensions: {ascensions}</span><span>AP: ×{ancPow.toFixed(1)}</span>
                  <span>Daily: F{dailyBest}</span><span>Rift: {riftUnlocked?"🌌 Open":"F100"}</span>
                </div>
                {/* Run history */}
                {runHistory.length>0&&(<div style={{marginTop:9}}>
                  <div style={{fontSize:9,color:"#334155",letterSpacing:2,marginBottom:5}}>RECENT RUNS</div>
                  {runHistory.slice(0,3).map((r,i)=>(
                    <div key={i} style={{...glass(),padding:8,marginBottom:5,display:"flex",alignItems:"center",gap:8}}>
                      <span style={{fontSize:16}}>{CLASSES[r.cls]?.icon}</span>
                      <div style={{flex:1}}>
                        <div style={{fontSize:10,color:"#c4b5fd"}}>{CLASSES[r.cls]?.name} · {WEAPONS[r.weapon]?.icon}</div>
                        <div style={{fontSize:9,color:"#64748b"}}>{r.date} · {r.kills} kills</div>
                      </div>
                      <div style={{textAlign:"right"}}>
                        <div style={{fontSize:14,fontWeight:"bold",color:"#818cf8"}}>F{r.floor}</div>
                        <div style={{fontSize:9,color:"#64748b"}}>💰{fmtN(r.gold)} 💜{fmtN(r.souls)}</div>
                      </div>
                    </div>
                  ))}
                </div>)}
              </div>)}

              {/* FORGE */}
              {hubTab==="forge"&&(<div>
                <p style={{textAlign:"center",fontSize:11,color:"#64748b",marginBottom:9}}>Permanent weapon upgrades. Costs scale exponentially.</p>
                {FORGE_ITEMS.map(item=><UpgradeRow key={item.id} item={item} level={forge[item.id]} currency="💰" balance={gold} onBuy={()=>buyForge(item)} accent="#4f46e5"/>)}
              </div>)}

              {/* RESEARCH */}
              {hubTab==="research"&&(<div>
                <p style={{textAlign:"center",fontSize:11,color:"#64748b",marginBottom:9}}>Permanent character upgrades. Costs scale exponentially.</p>
                {RESEARCH_ITEMS.map(item=><UpgradeRow key={item.id} item={item} level={research[item.id]} currency="💜" balance={souls} onBuy={()=>buyResearch(item)} accent="#7c3aed"/>)}
              </div>)}

              {/* PRESTIGE */}
              {hubTab==="prestige"&&(<div>
                <div style={{...glass({border:"1px solid #34d399"}),padding:10,marginBottom:11,textAlign:"center"}}>
                  <div style={{fontSize:13,color:"#34d399",fontWeight:"bold",marginBottom:3}}>🎀 Astral Ribbons: {ribbons}</div>
                  <div style={{fontSize:10,color:"#64748b"}}>Earned by reaching Floor 100. Costs scale with owned skills.</div>
                </div>
                {PRESTIGE_SKILLS.map(sk=>{
                  const owned=prestige.includes(sk.id),dCost=sk.baseCost+prestige.length,can=!owned&&ribbons>=dCost;
                  return(
                    <div key={sk.id} style={{...glass({border:`1px solid ${owned?"#34d399":"rgba(99,102,241,.15)"}`}),padding:10,display:"flex",alignItems:"center",gap:10,marginBottom:6}}>
                      <span style={{fontSize:20}}>{sk.icon}</span>
                      <div style={{flex:1}}>
                        <div style={{fontWeight:"bold",color:owned?"#34d399":"#e2e8f0",fontSize:11}}>{sk.name}{owned?" ✓":""}</div>
                        <div style={{fontSize:10,color:"#64748b"}}>{sk.desc}</div>
                        {!owned&&<div style={{fontSize:8,color:"#475569",marginTop:1}}>🎀{dCost} (base {sk.baseCost}+{prestige.length})</div>}
                      </div>
                      <button className="btn" onClick={()=>buyPrestige(sk)} disabled={owned||!can}
                        style={{padding:"9px 11px",borderRadius:8,fontSize:11,minWidth:50,minHeight:44,
                          background:owned?"#1e293b":can?"linear-gradient(135deg,#065f46,#059669)":"#1e293b",
                          color:can?"#34d399":owned?"#34d399":"#374151",border:`1px solid ${owned||can?"#34d399":"#374151"}`,
                          fontFamily:"inherit",fontWeight:"bold"}}>
                        {owned?"✓":`🎀${dCost}`}
                      </button>
                    </div>
                  );
                })}
              </div>)}

              {/* RIFT */}
              {hubTab==="rift"&&(<div>
                {!riftUnlocked&&<div style={{textAlign:"center",padding:30,color:"#334155",fontSize:13}}>🔒 Reach Floor 100 to unlock the Infinite Rift.</div>}
                {riftUnlocked&&(<>
                  <div style={{...glass({border:"1px solid rgba(124,58,237,.4)"}),padding:11,marginBottom:10}}>
                    <div style={{fontSize:12,color:"#c4b5fd",fontWeight:"bold",marginBottom:3}}>☠️ Corruption: {corruption}</div>
                    <div style={{fontSize:10,color:"#64748b",marginBottom:9}}>+30%/tier enemy power · +25%/tier loot</div>
                    <div style={{display:"flex",gap:9}}>
                      <button className="btn" onClick={()=>setCorruption(c=>Math.max(0,c-1))} style={{flex:1,borderRadius:9,background:"#1e293b",color:"#94a3b8",fontSize:12}}>− Lower</button>
                      <button className="btn" onClick={()=>setCorruption(c=>c+1)} style={{flex:1,borderRadius:9,background:"linear-gradient(135deg,#4c1d95,#7c3aed)",color:"#fff",fontSize:12}}>+ Raise</button>
                    </div>
                  </div>
                  <div style={{...glass(),padding:10,fontSize:10,color:"#64748b"}}>
                    <div style={{color:"#6366f1",marginBottom:4,fontSize:9,letterSpacing:1}}>🏆 LEADERBOARD</div>
                    <div>Best: F{bestFloor}</div><div>Daily: F{dailyBest}</div>
                    <div>AP: ×{ancPow.toFixed(2)}</div><div>Runs: {totalRuns}</div>
                  </div>
                </>)}
              </div>)}
            </div>
          )}

          {/* ════════ ASCEND ════════ */}
          {screen==="ascend"&&(
            <div style={{animation:"slideUp .3s ease",textAlign:"center",padding:"28px 10px"}}>
              <div style={{fontSize:40,marginBottom:8}}>🌅</div>
              <div style={{fontSize:17,fontWeight:"bold",background:"linear-gradient(90deg,#fbbf24,#f59e0b)",WebkitBackgroundClip:"text",WebkitTextFillColor:"transparent",marginBottom:5}}>Ascension {ascensions+1}</div>
              <p style={{fontSize:11,color:"#64748b",maxWidth:280,margin:"0 auto 12px"}}>All gold, souls, forge and research are sacrificed. Ancient Power grows ×2.5 permanently.</p>
              <div style={{fontSize:15,color:"#f472b6",marginBottom:18}}>New AP: ×{(ancPow*2.5).toFixed(2)}</div>
              <div style={{display:"flex",gap:10,justifyContent:"center"}}>
                <button className="btn" onClick={()=>setScreen("hub")} style={{padding:"11px 18px",borderRadius:10,background:"#1e293b",color:"#94a3b8",fontSize:12}}>← Back</button>
                <button className="btn" onClick={doAscend} style={{padding:"11px 18px",borderRadius:10,background:"linear-gradient(135deg,#b45309,#78350f)",color:"#fef3c7",fontSize:12,fontWeight:"bold"}}>🌅 Ascend</button>
              </div>
            </div>
          )}

          {/* ════════ RUN ════════ */}
          {screen==="run"&&g&&(
            <div style={{animation:"slideUp .25s ease",display:"flex",flexDirection:"column",gap:7}}>

              {/* COMBAT ARENA — hero and enemy are BIG and center-focused */}
              <div style={{...glass({
                border:`1px solid ${burstReady?"rgba(245,158,11,.5)":lowHpPulse?"rgba(239,68,68,.4)":`rgba(${biome.fogC},.3)`}`,
                boxShadow:burstReady?"0 0 28px rgba(245,158,11,.2)":lowHpPulse?"0 0 20px rgba(239,68,68,.15)":"none",
              }),padding:14,position:"relative",overflow:"hidden",transition:"all .4s"}}>

                {/* Particles */}
                <div style={{position:"absolute",inset:0,pointerEvents:"none",overflow:"hidden",zIndex:12}}>
                  {particlesOn&&ptcls.map(p=>(
                    <div key={p.id} style={{position:"absolute",left:p.x,top:p.y,width:p.size,height:p.size,
                      borderRadius:"50%",background:p.color,opacity:p.opacity,transform:"translate(-50%,-50%)",
                      boxShadow:`0 0 ${p.size*2}px ${p.color}88`,
                      animation:`particleDrift ${p.dur}ms ease-out forwards`,
                      "--dx":`${p.dx}px`,"--dy":`${p.dy}px`}}/>
                  ))}
                </div>

                {/* Floating text */}
                {floats.map(f=>(
                  <div key={f.id} style={{position:"absolute",left:`${f.x}%`,top:"22%",color:f.color,
                    fontWeight:"bold",fontSize:f.big?20:15,
                    animation:f.big?"floatBig 1.4s ease forwards":"floatUp 1.3s ease forwards",
                    pointerEvents:"none",zIndex:13,textShadow:`0 0 12px ${f.color}`,whiteSpace:"nowrap"}}>
                    {f.text}
                  </div>
                ))}

                {/* BLESSING OVERLAY */}
                {g.phase==="blessing"&&g.blessingChoices&&(
                  <div style={{position:"absolute",inset:0,background:"rgba(5,6,14,.96)",borderRadius:14,zIndex:25,display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center",padding:14}}>
                    <div style={{fontSize:11,color:"#fbbf24",fontWeight:"bold",marginBottom:12,letterSpacing:2}}>✨ CHOOSE A BLESSING</div>
                    <div style={{display:"flex",gap:8,flexWrap:"wrap",justifyContent:"center"}}>
                      {g.blessingChoices.map(b=>(
                        <button key={b.id} className="btn" onClick={()=>chooseBlessing(b)}
                          style={{background:"rgba(99,102,241,.15)",border:"2px solid #6366f1",borderRadius:12,padding:11,color:"#e2e8f0",textAlign:"center",minWidth:95,maxWidth:110,boxShadow:"0 0 14px #6366f144",fontFamily:"inherit"}}>
                          <div style={{fontSize:22}}>{b.icon}</div>
                          <div style={{fontSize:10,fontWeight:"bold",color:"#c4b5fd",marginTop:3}}>{b.name}</div>
                          <div style={{fontSize:9,color:"#64748b",marginTop:2}}>{b.desc}</div>
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                {/* RELIC CHEST */}
                {g.phase==="relic"&&g.relicChoices&&<RelicChestReveal choices={g.relicChoices} onChoose={chooseRelic} onSkip={skipRelic}/>}

                {/* MILESTONE RELIC */}
                {g.phase==="milestone"&&g.milestoneRelic&&(
                  <div style={{position:"absolute",inset:0,background:"rgba(5,6,14,.97)",borderRadius:14,zIndex:25,display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center",padding:14,animation:"milestoneGlow 2s infinite"}}>
                    <div style={{fontSize:9,color:"#f59e0b",letterSpacing:4,marginBottom:8}}>⚠️ BOSS RELIC</div>
                    <div style={{fontSize:44,marginBottom:6,filter:"drop-shadow(0 0 22px #f59e0b)"}}>{g.milestoneRelic.icon}</div>
                    <div style={{fontSize:14,fontWeight:"bold",background:"linear-gradient(90deg,#fbbf24,#f59e0b)",WebkitBackgroundClip:"text",WebkitTextFillColor:"transparent",marginBottom:4}}>{g.milestoneRelic.name}</div>
                    <div style={{fontSize:10,color:"#94a3b8",marginBottom:3,textAlign:"center",maxWidth:220}}>{g.milestoneRelic.desc}</div>
                    <div style={{fontSize:9,color:"#ef4444",marginBottom:12}}>Powerful — with a trade-off.</div>
                    <div style={{display:"flex",gap:9}}>
                      <button className="btn" onClick={()=>chooseMilestone(g.milestoneRelic)} style={{padding:"10px 16px",borderRadius:10,background:"linear-gradient(135deg,#b45309,#f59e0b)",color:"#fff",fontSize:12,fontWeight:"bold"}}>Accept</button>
                      <button className="btn" onClick={skipMilestone} style={{padding:"10px 16px",borderRadius:10,background:"#1e293b",color:"#64748b",fontSize:11}}>Decline</button>
                    </div>
                  </div>
                )}

                {/* DEAD OVERLAY */}
                {g.phase==="dead"&&(
                  <div style={{position:"absolute",inset:0,background:"rgba(5,6,14,.97)",borderRadius:14,zIndex:25,display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center",padding:14}}>
                    <div style={{fontSize:38,marginBottom:6}}>💀</div>
                    <div style={{fontSize:16,color:"#f87171",fontWeight:"bold",marginBottom:3}}>The Hero Has Fallen</div>
                    <div style={{fontSize:10,color:"#64748b",marginBottom:7}}>Floor {g.floor} · {g.killCount} kills</div>
                    <div style={{fontSize:12,color:"#fbbf24",marginBottom:1}}>+💰{fmtN(0|g.runGold)}</div>
                    <div style={{fontSize:12,color:"#a78bfa",marginBottom:3}}>+💜{fmtN(0|g.runSouls)}</div>
                    {g.isDaily&&<div style={{fontSize:11,color:"#34d399",marginBottom:7}}>📅 Daily: F{g.floor}</div>}
                    <button className="btn" onClick={returnToHub} style={{padding:"11px 24px",borderRadius:10,background:"linear-gradient(135deg,#4f46e5,#7c3aed)",color:"#fff",fontSize:13,fontWeight:"bold"}}>Return to Hub</button>
                  </div>
                )}

                {/* ── HERO (large, centered) ── */}
                <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",gap:10,marginBottom:12}}>
                  {/* Hero card */}
                  <div style={{flex:1,background:"rgba(10,14,26,.85)",borderRadius:14,padding:11,
                    animation:heroShake?"shakeN .15s ease":"none",
                    border:`2px solid ${heroHpPct<20?"rgba(239,68,68,.5)":"rgba(99,102,241,.22)"}`,
                    transition:"border-color .3s",textAlign:"center"}}>
                    <div style={{fontSize:40,lineHeight:1,marginBottom:4,filter:`drop-shadow(0 0 8px ${biome.accent}66)`}}>{CLASSES[hero?.cls]?.icon}</div>
                    <div style={{fontSize:11,fontWeight:"bold",color:"#c4b5fd"}}>{CLASSES[hero?.cls]?.name}</div>
                    <div style={{fontSize:9,color:"#64748b"}}>{WEAPONS[hero?.weapon]?.icon} {WEAPONS[hero?.weapon]?.name}</div>
                    {/* Thick HP bar */}
                    <div style={{marginTop:7,height:14,background:"#0a0e1a",borderRadius:7,overflow:"hidden",border:"1px solid rgba(255,255,255,.06)",
                      animation:heroShake?"pulseHP .15s ease":"none"}}>
                      <div className="hpfill" style={{width:`${heroHpPct}%`,height:"100%",
                        background:heroHpPct<20?"linear-gradient(90deg,#b91c1c,#ef4444)":heroHpPct<50?"linear-gradient(90deg,#b45309,#f59e0b)":"linear-gradient(90deg,#15803d,#22c55e)",
                        boxShadow:heroHpPct<20?"0 0 12px #ef444488":"0 0 8px rgba(34,197,94,.3)"}}/>
                    </div>
                    <div style={{fontSize:9,color:"#94a3b8",marginTop:3}}>{Math.max(0,0|hero?.hp||0)} / {hero?.maxHp}</div>
                    {hero?.cls==="paladin"&&hero?.holyShield>0&&(
                      <div style={{marginTop:4}}>
                        <div style={{height:5,background:"#0a0e1a",borderRadius:3,overflow:"hidden"}}>
                          <div className="hpfill" style={{width:`${pct(hero.holyShield,hero.holyShieldMax)}%`,height:"100%",background:"linear-gradient(90deg,#ca8a04,#fde68a)"}}/>
                        </div>
                        <div style={{fontSize:8,color:"#fde68a",marginTop:1}}>🛡️ {0|hero.holyShield}</div>
                      </div>
                    )}
                    {hero?.cls==="shadowblade"&&<div style={{fontSize:9,marginTop:3,color:hero.stealthReady?"#a78bfa":"#475569"}}>{hero.stealthReady?"🌑 STEALTH!":`🌑 ${hero.stealthHits||0}/3`}</div>}
                    <AttackBar prog={heroBarPct()} color={biome.accent} label="ATK" stunned={false}/>
                    <div style={{display:"flex",justifyContent:"center",gap:6,marginTop:5,fontSize:9}}>
                      <span style={{color:"#fbbf24"}}>⚔️{hero?.atk}</span>
                      <span style={{color:"#60a5fa"}}>🛡️{hero?.def}</span>
                    </div>
                    {(hero?.snapFreezeStacks||0)>0&&<div style={{fontSize:8,color:"#93c5fd",marginTop:1}}>🧊×{hero.snapFreezeStacks}</div>}
                  </div>

                  <div style={{color:"#334155",fontSize:12,fontWeight:"bold",flexShrink:0}}>VS</div>

                  {/* Enemy card */}
                  {enemy?(
                    <div style={{flex:1,background:"rgba(18,8,32,.85)",borderRadius:14,padding:11,
                      animation:enemyShake?"shakeN .12s ease":"none",
                      border:`2px solid ${enemy.isBoss?"rgba(245,158,11,.4)":enemy.isElite?"rgba(244,114,182,.35)":"rgba(239,68,68,.22)"}`,
                      transition:"border-color .3s",textAlign:"center"}}>
                      <div style={{fontSize:40,lineHeight:1,marginBottom:4,
                        filter:`drop-shadow(0 0 10px ${enemy.isBoss?"#f59e0b":enemy.isElite?"#f472b6":"#ef4444"}66)`}}>{enemy.icon}</div>
                      {enemy.isBoss?(
                        <div style={{fontSize:11,fontWeight:"bold",background:"linear-gradient(90deg,#fbbf24,#f59e0b)",WebkitBackgroundClip:"text",WebkitTextFillColor:"transparent"}}>{enemy.name}</div>
                      ):(
                        <div style={{fontSize:11,fontWeight:"bold",color:"#f87171"}}>{enemy.name}</div>
                      )}
                      {enemy.isElite&&<div style={{fontSize:8,color:"#fb923c",letterSpacing:1}}>⚡ ELITE</div>}
                      {enemy.isBoss&&<div style={{fontSize:8,color:"#f59e0b",letterSpacing:1}}>👑 WARDEN</div>}
                      {/* Thick HP bar */}
                      <div style={{marginTop:7,height:14,background:"#0a0e1a",borderRadius:7,overflow:"hidden",border:"1px solid rgba(255,255,255,.06)"}}>
                        <div className="hpfill" style={{width:`${pct(Math.max(0,enemy.hp),enemy.maxHp)}%`,height:"100%",
                          background:enemy.isBoss?"linear-gradient(90deg,#92400e,#f59e0b,#f472b6)":enemy.isElite?"linear-gradient(90deg,#9d174d,#f472b6)":"linear-gradient(90deg,#991b1b,#ef4444)",
                          boxShadow:enemy.isBoss?"0 0 12px #f59e0b88":"none"}}/>
                      </div>
                      <div style={{fontSize:9,color:"#94a3b8",marginTop:3}}>{Math.max(0,0|enemy.hp)} / {enemy.maxHp}</div>
                      {/* Status icons floating next to enemy */}
                      <div style={{display:"flex",justifyContent:"center",gap:4,marginTop:4,fontSize:14}}>
                        {enemy.poisoned&&<span title="Poisoned" style={{filter:"drop-shadow(0 0 4px #a3e635)"}}>☠️</span>}
                        {enemy.frozen&&<span title={`Frozen (${enemy.frozenLeft})`} style={{filter:"drop-shadow(0 0 4px #93c5fd)"}}>❄️</span>}
                        {enemy.snapFreezeReady&&<span title="Snap Freeze ready!" style={{filter:"drop-shadow(0 0 6px #fbbf24)"}}>⚡</span>}
                        {enemy.stunned&&<span title="Stunned" style={{filter:"drop-shadow(0 0 4px #f59e0b)"}}>💫</span>}
                      </div>
                      {!enemy.poisoned&&enemy.poisonBuildup>4&&<div style={{fontSize:8,color:"#86efac"}}>🟢{0|enemy.poisonBuildup}%</div>}
                      {!enemy.frozen&&enemy.frostBuildup>4&&<div style={{fontSize:8,color:"#bfdbfe"}}>🔵{0|enemy.frostBuildup}%</div>}
                      <AttackBar prog={enemyBarPct()} color="#ef4444" label="ATK" stunned={enemy.stunned}/>
                    </div>
                  ):(
                    <div style={{flex:1,display:"flex",alignItems:"center",justifyContent:"center",color:"#334155",fontSize:11}}>…advancing…</div>
                  )}
                </div>

                {/* Run stats row */}
                <div style={{display:"flex",justifyContent:"space-between",fontSize:9,color:"#475569",marginBottom:8}}>
                  <span>Floor {g.floor} · {g.killCount} kills</span>
                  <span style={{color:biome.accent}}>{biome.name}</span>
                  {g.goldPerMin>0&&<span>{fmtN(g.goldPerMin)}/m💰</span>}
                </div>

                {/* Burst bar */}
                <div>
                  <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:3}}>
                    <span style={{fontSize:9,color:"#64748b"}}>⚡ Burst Charge {pct(g.burstCharge,g.burstMax)}%</span>
                    <button onClick={()=>setAutoBurst(a=>!a)} style={{fontSize:8,padding:"2px 7px",borderRadius:4,border:`1px solid ${autoBurst?"#f59e0b":"#334155"}`,background:autoBurst?"rgba(245,158,11,.15)":"transparent",color:autoBurst?"#fbbf24":"#475569",cursor:"pointer",fontFamily:"inherit",minHeight:0}}>AUTO {autoBurst?"ON":"OFF"}</button>
                  </div>
                  <div style={{height:10,background:"#0a0e1a",borderRadius:5,overflow:"hidden",marginBottom:6}}>
                    <div className="hpfill" style={{width:`${pct(g.burstCharge,g.burstMax)}%`,height:"100%",
                      background:burstReady?"linear-gradient(90deg,#f59e0b,#fcd34d)":"linear-gradient(90deg,#4f46e5,#818cf8)",
                      boxShadow:burstReady?"0 0 14px #fbbf24":"none"}}/>
                  </div>
                  <button className="btn" onClick={fireBurst} disabled={!burstReady||g.phase!=="fighting"}
                    style={{width:"100%",padding:"11px 0",borderRadius:10,fontSize:13,fontWeight:"bold",letterSpacing:1,
                      background:burstReady?"linear-gradient(90deg,#b45309,#f59e0b)":"#1e293b",
                      color:burstReady?"#fff":"#374151",border:"none",
                      animation:burstReady?"burstPulse 1.1s infinite":"none",
                      boxShadow:burstReady?"0 0 20px #f59e0b55":"none"}}>
                    {burstReady?"⚡ BURST STRIKE!":"⚡ Charging..."}
                  </button>
                </div>
              </div>

              {/* Relics + Blessings */}
              {((hero?.relics?.length||0)+(hero?.blessings?.length||0))>0&&(
                <div style={{...glass(),padding:9}}>
                  {(hero?.relics?.length||0)>0&&(<>
                    <div style={{fontSize:8,color:"#4f46e5",letterSpacing:2,marginBottom:4}}>RELICS</div>
                    <div style={{display:"flex",flexWrap:"wrap",gap:4,marginBottom:(hero?.blessings?.length||0)>0?6:0}}>
                      {hero.relics.map((r,i)=>(
                        <div key={i} title={r.desc} style={{background:"rgba(255,255,255,.04)",borderRadius:5,padding:"3px 7px",fontSize:9,border:`1px solid ${rc(r.rarity)}`,color:rc(r.rarity)}}>{r.icon} {r.name}</div>
                      ))}
                    </div>
                  </>)}
                  {(hero?.blessings?.length||0)>0&&(<>
                    <div style={{fontSize:8,color:"#fbbf24",letterSpacing:2,marginBottom:4}}>BLESSINGS</div>
                    <div style={{display:"flex",flexWrap:"wrap",gap:4}}>
                      {hero.blessings.map((bid,i)=>{const b=BLESSINGS.find(x=>x.id===bid);return b?<div key={i} style={{background:"rgba(255,255,255,.04)",borderRadius:5,padding:"3px 7px",fontSize:9,color:"#fbbf24"}}>{b.icon} {b.name}</div>:null;})}
                    </div>
                  </>)}
                </div>
              )}

              {/* Combat log */}
              <div style={{...glass(),padding:9}}>
                <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:5}}>
                  <div style={{fontSize:8,color:"#334155",letterSpacing:2}}>LOG</div>
                  <div style={{display:"flex",gap:3}}>
                    {["all","big","relics"].map(f=>(
                      <button key={f} onClick={()=>setLogFilter(f)} style={{fontSize:7,padding:"2px 5px",borderRadius:3,border:`1px solid ${logFilter===f?"#6366f1":"#1e293b"}`,background:logFilter===f?"rgba(99,102,241,.2)":"transparent",color:logFilter===f?"#c4b5fd":"#475569",cursor:"pointer",fontFamily:"inherit",minHeight:0}}>
                        {f==="all"?"All":f==="big"?"Big":"Relics"}
                      </button>
                    ))}
                  </div>
                </div>
                {filteredLog.map((e,i)=>(
                  <div key={i} style={{fontSize:9,color:i===0?e.type==="big"?"#fbbf24":e.type==="relic"?"#34d399":"#e2e8f0":"#475569",marginBottom:2,lineHeight:1.3}}>{e.text}</div>
                ))}
              </div>

              <button className="btn" onClick={returnToHub} style={{width:"100%",padding:"10px 0",borderRadius:10,background:"rgba(255,255,255,.04)",border:"1px solid #1e293b",color:"#64748b",fontSize:11}}>
                💀 Collapse — Return to Hub
              </button>
            </div>
          )}

          {screen==="ascend"&&null}
        </div>

        {/* ── PERSISTENT BOTTOM NAVIGATION (Hub only) ── */}
        {screen==="hub"&&(
          <div style={{position:"sticky",bottom:0,left:0,right:0,zIndex:40,
            background:"rgba(7,8,15,0.95)",backdropFilter:"blur(18px)",
            borderTop:"1px solid rgba(99,102,241,0.2)",
            display:"flex",justifyContent:"space-around",alignItems:"center",padding:"6px 0",flexShrink:0}}>
            {NAV.map(n=>{
              const act=hubTab===n.id;
              return(
                <button key={n.id} onClick={()=>setHubTab(n.id)}
                  style={{display:"flex",flexDirection:"column",alignItems:"center",gap:2,
                    padding:"6px 10px",borderRadius:10,border:"none",cursor:"pointer",
                    background:act?"rgba(99,102,241,.18)":"transparent",
                    color:act?"#c4b5fd":"#475569",fontFamily:"inherit",
                    transition:"all .18s",minWidth:52,minHeight:44,
                    boxShadow:act?"0 0 12px rgba(99,102,241,.3)":"none"}}>
                  <span style={{fontSize:20,lineHeight:1}}>{n.icon}</span>
                  <span style={{fontSize:9,fontWeight:act?"bold":"normal",letterSpacing:.5}}>{n.label}</span>
                </button>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
